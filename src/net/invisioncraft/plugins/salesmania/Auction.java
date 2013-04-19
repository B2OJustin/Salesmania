/*
This file is part of Salesmania.

    Salesmania is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Salesmania is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Salesmania.  If not, see <http://www.gnu.org/licenses/>.
*/

package net.invisioncraft.plugins.salesmania;

import net.invisioncraft.plugins.salesmania.configuration.AuctionSettings;
import net.invisioncraft.plugins.salesmania.configuration.Locale;
import net.invisioncraft.plugins.salesmania.event.auction.*;
import net.invisioncraft.plugins.salesmania.util.ItemManager;
import net.invisioncraft.plugins.salesmania.worldgroups.WorldGroup;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Auction {
    Salesmania plugin;
    Economy economy;
    AuctionSettings auctionSettings;
    WorldGroup worldGroup;
    AuctionQueue auctionQueue;

    private boolean isRunning = false;

    private OfflinePlayer owner;
    private OfflinePlayer winner;
    private OfflinePlayer lastWinner;
    private double bid = 0;
    private double lastBid = 0;

    private double startTax = 0;
    private double endTax = 0;

    private ItemStack itemStack;

    private int timeRemaining = 0;
    private int position = 0;

    public static enum AuctionStatus {
        OVER_MAX,
        UNDER_MIN,
        SUCCESS,
        FAILURE,
        WINNING,
        NOT_RUNNING,
        QUEUE_FULL,
        PLAYER_QUEUE_FULL,
        OWNER,
        CANT_AFFORD_TAX,
        QUEUE_SUCCESS,
        COOLDOWN_SUCCESS
    }

    private HashMap<String, String> tokenMap;
    private static Pattern tokenPattern;
    private static String[] tokens = new String[] {
            "%owner%", "%quantity%", "%item%", "%durability%",
            "%bid%", "%winner%", "%enchantinfo%", "%timeremaining%", "%position%",
            "%booktitle%", "%bookauthor%", "%displayname%"
    };

    public Auction(Salesmania plugin, WorldGroup worldGroup) {
        this.plugin = plugin;
        auctionSettings = plugin.getSettings().getAuctionSettings();

        String patternString = "(";
        for(String token : tokens) {
            patternString += token + "|";
        }
        patternString += ")";
        tokenPattern = Pattern.compile(patternString);
        tokenMap = new HashMap<String, String>();
        economy = plugin.getEconomy();
        timeRemaining = auctionSettings.getDefaultTime();
        this.auctionQueue = worldGroup.getAuctionQueue();
    }

    public Auction(Salesmania plugin, WorldGroup worldGroup, OfflinePlayer owner, OfflinePlayer winner, ItemStack itemStack, double bid) {
        this(plugin, worldGroup);
        this.owner = owner;
        this.itemStack = itemStack;
        this.bid = bid;
        this.winner = winner;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public OfflinePlayer getWinner() {
        return winner;
    }

    public OfflinePlayer getOwner() {
        return owner;
    }

    public double getBid() {
        return bid;
    }

    public double getMaxBid() {
        return bid + auctionSettings.getMaxIncrement();
    }

    public double getMinBid() {
        if(lastBid == 0) return bid;
        else return bid + auctionSettings.getMinIncrement();
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public AuctionStatus queue(Player player, ItemStack itemStack, double startBid)  {
        AuctionStatus checkResult = performChecks(player, startBid);
        if(checkResult != AuctionStatus.SUCCESS) return checkResult;

        bid = startBid;
        this.itemStack = itemStack.clone();
        owner = player;
        plugin.getAuctionIgnoreList().setIgnore(player, false);
        worldGroup = plugin.getWorldGroupManager().getGroup(player);
        AuctionQueue auctionQueue = worldGroup.getAuctionQueue();

        position = auctionQueue.size()+1;
        updateInfoTokens();
        if(auctionQueue.add(this)) {
            if(auctionQueue.size() != 1) return AuctionStatus.QUEUE_SUCCESS;
            if(auctionQueue.isCooldown()) return AuctionStatus.COOLDOWN_SUCCESS;
            else return AuctionStatus.SUCCESS;
        }
        else {
            plugin.getLogger().warning("Failed to add auction to queue.");
            plugin.getLogger().info(String.format(
                    "Player: %s\n ItemStack: %s\n Starting Bid: %,f",
                    player.getName(), itemStack.toString(), startBid));
            return AuctionStatus.FAILURE;
        }
    }

    protected AuctionStatus start() {
        isRunning = true;
        plugin.getServer().getPluginManager().callEvent(new AuctionStartEvent(this));
        return AuctionStatus.SUCCESS;
    }

    private AuctionStatus performChecks(Player player, double startBid) {
        AuctionQueue auctionQueue = plugin.getWorldGroupManager().getGroup(player).getAuctionQueue();
        String ecoWorld = getWorldGroup().getWorlds().get(0).getName();
        if(auctionQueue.size() >= auctionSettings.getMaxQueueSize()) return AuctionStatus.QUEUE_FULL;
        if(auctionQueue.playerSize(player) >= auctionSettings.getMaxQueuePerPlayer()) return AuctionStatus.PLAYER_QUEUE_FULL;
        if(startBid < auctionSettings.getMinStart()) return AuctionStatus.UNDER_MIN;
        if(startBid > auctionSettings.getMaxStart()) return AuctionStatus.OVER_MAX;

        // Tax
        if(auctionSettings.getStartTax() != 0) {
            startTax = auctionSettings.getStartTax();
            if(auctionSettings.isStartTaxPercent()) {
                startTax = (startTax / 100) * startBid;
            }
            if(!economy.has(player.getName(), ecoWorld, startTax)) return AuctionStatus.CANT_AFFORD_TAX;
        }
        return AuctionStatus.SUCCESS;
    }

    public AuctionStatus bid(Player player, double bid) {
        if(!isRunning) return AuctionStatus.NOT_RUNNING;
        if(player == owner) return AuctionStatus.OWNER;
        if(winner != null && winner == player) return AuctionStatus.WINNING;
        if(bid > getMaxBid()) return AuctionStatus.OVER_MAX;
        if(bid < getMinBid()) return AuctionStatus.UNDER_MIN;

        lastWinner = winner;
        lastBid = this.bid;

        winner = player;
        this.bid = bid;
        plugin.getAuctionIgnoreList().setIgnore(player, false);
        updateInfoTokens();
        plugin.getServer().getPluginManager().callEvent(new AuctionBidEvent(this));
        return AuctionStatus.SUCCESS;
    }

    public void end() {
        if(isRunning()) {
            // Tax
            if(auctionSettings.getEndTax() != 0) {
                endTax = auctionSettings.getEndTax();
                if(auctionSettings.isEndTaxPercent()) {
                    endTax = (endTax / 100) * getBid();
                }
            }

            plugin.getServer().getPluginManager().callEvent(new AuctionEndEvent(this));
            isRunning = false;
        }
    }

    public AuctionStatus cancel() {
        if(!isRunning()) return AuctionStatus.NOT_RUNNING;
        plugin.getServer().getPluginManager().callEvent(new AuctionCancelEvent(this));
        isRunning = false;
        return AuctionStatus.SUCCESS;
    }

    private float getDurability() {
        float dur = ((float) itemStack.getType().getMaxDurability() - (float) itemStack.getDurability()) / (float) itemStack.getType().getMaxDurability();
        return dur * 100;
    }

    public ArrayList<String> infoReplace(ArrayList<String> infoList) {
        ArrayList<String> newInfoList = new ArrayList<>();

        for(String string : infoList) {
            // Remove unused lines
            if(itemStack.getEnchantments().isEmpty() && !(itemStack.getItemMeta() instanceof EnchantmentStorageMeta)) {
                if(string.contains("%enchantinfo%")) continue;
            }

            if(itemStack.getType().getMaxDurability() == 0 && string.contains("%durability%")) continue;

            // Remove enchant display from spawner
            if(itemStack.getType() == Material.MOB_SPAWNER && string.contains("%enchantinfo%")) continue;

            // Remove display and book lines from non-books
            if(itemStack.getType() != Material.WRITTEN_BOOK) {
                if(string.contains("%booktitle%") | string.contains("%bookauthor%")) continue;
            }

            // Remove display name line from non-renamed items
            if(!itemStack.getItemMeta().hasDisplayName() && string.contains("%displayname%")) continue;

            // Replace tokens
            StringBuffer buffer = new StringBuffer();
            Matcher matcher = tokenPattern.matcher(string);
            String value;
            while(matcher.find()) {
                value = tokenMap.get(matcher.group());
                if(value != null) matcher.appendReplacement(buffer, value);
            }
            matcher.appendTail(buffer);
            newInfoList.add(buffer.toString());

        }
        return newInfoList;
    }

    public String infoReplace(String infoMessage) {
        ArrayList<String> list = new ArrayList<>();
        list.add(infoMessage);
        return infoReplace(list).get(0);
    }

    public String enchantReplace(String enchantMessage, String enchant, String enchantFormat, Locale locale) {
        ArrayList<String> list = new ArrayList<>();
        list.add(enchantMessage);
        return enchantReplace(list, enchant, enchantFormat, locale).get(0);
    }

    // TODO hmm...
    public ArrayList<String> enchantReplace(ArrayList<String> infoList, String enchant, String enchantInfo, Locale locale) {
        if(ItemManager.getEnchants(itemStack).isEmpty()) {
            infoList.remove("%enchantinfo%");
            return infoList;
        }
        if(!infoList.contains("%enchantinfo%")) return infoList;

        for(Map.Entry<Enchantment, Integer> ench : ItemManager.getEnchants(itemStack).entrySet()) {
            enchant += enchantInfo.replace("%enchantlvl%", String.valueOf(ench.getValue()));
            if(locale != null) enchant = enchant.replace("%enchant%", locale.getMessage("Enchantment." + ench.getKey().getName()));
            else enchant = enchant.replace("%enchant%", ench.getKey().getName());
        }

        infoList.set(infoList.indexOf("%enchantinfo%"), enchant);
        return infoList;
    }

    public void updateInfoTokens() {
        tokenMap.clear();
        tokenMap.put("%owner%", owner.getName());
        tokenMap.put("%quantity%", String.valueOf(itemStack.getAmount()));
        tokenMap.put("%item%", ItemManager.getName(itemStack));

        if(itemStack.getType() == Material.WRITTEN_BOOK) {
            BookMeta bookMeta = (BookMeta)itemStack.getItemMeta();
            tokenMap.put("%booktitle%", bookMeta.getTitle());
            tokenMap.put("%bookauthor%", bookMeta.getAuthor());
        }

         // Renamed items (%displayname% can be used regardless of config)
        else if(itemStack.getItemMeta().hasDisplayName()) {
            tokenMap.put("%displayname%", itemStack.getItemMeta().getDisplayName());
        }

        else tokenMap.put("%item%", ItemManager.getName(itemStack));

        tokenMap.put("%durability%", String.format("%.2f%%", getDurability()));
        tokenMap.put("%bid%", String.format("%,.2f", bid));
        tokenMap.put("%timeremaining%", String.valueOf(timeRemaining));
        tokenMap.put("%position%", String.valueOf(position));
        if(winner != null) tokenMap.put("%winner%", winner.getName());
        else tokenMap.put("%winner%", "None");
    }

    public int getTimeRemaining() {
        return timeRemaining;
    }

    public void setTimeRemaining(int time) {
        timeRemaining = time;
    }

    protected void timerTick() {
        plugin.getServer().getPluginManager().callEvent(new AuctionTimerEvent(this));
        if(isRunning) {
            timeRemaining -= 1;
            if (timeRemaining == 0) {
                end();
            }
        }
    }

    public Salesmania getPlugin() {
        return plugin;
    }

    public double getLastBid() {
        return lastBid;
    }

    public OfflinePlayer getLastWinner() {
        return lastWinner;
    }

    public double getStartTax() {
        return startTax;
    }

    public double getEndTax() {
        return endTax;
    }

    public WorldGroup getWorldGroup() {
        return auctionQueue.getWorldGroup();
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}