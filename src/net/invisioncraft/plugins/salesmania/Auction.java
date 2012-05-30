package net.invisioncraft.plugins.salesmania;

import net.invisioncraft.plugins.salesmania.configuration.AuctionSettings;
import net.invisioncraft.plugins.salesmania.event.AuctionEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Owner: Justin
 * Date: 5/17/12
 * Time: 3:59 PM
 */
public class Auction {
    private static long TICKS_PER_SECOND = 20;
    Salesmania plugin;
    AuctionSettings auctionSettings;

    private boolean isRunning = false;
    private boolean inCooldown = false;

    private Player currentWinner;
    private Player owner;
    private float currentBid;

    private ItemStack itemStack;

    private long timeRemaining = 0;

    private Runnable cooldownRunnable = new Runnable() {
        @Override
        public void run() {
            inCooldown = false;
        }
    };

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if(isRunning) {
                timeRemaining -= 1;
                if (timeRemaining == 0) end();
                callTimerEvent();
            }
            else {
                Bukkit.getServer().getScheduler().cancelTask(timerID);
            }
        }
    };

    private int timerID;

    public static enum AuctionStatus {
        OVER_MAX,
        UNDER_MIN,
        SUCCESS,
        FAILURE,
        RUNNING,
        COOLDOWN,
        WINNING,
        NOT_RUNNING,
        CANCELED
    }

    public Auction(Salesmania plugin) {
        this.plugin = plugin;
        auctionSettings = plugin.getSettings().getAuctionSettings();
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isInCooldown() {
        return inCooldown;
    }

    public Player getWinner() {
        return currentWinner;
    }

    public Player getOwner() {
        return owner;
    }

    public float getCurrentBid() {
        return currentBid;
    }

    public float getMaxBid() {
        return currentBid + auctionSettings.getMaxIncrement();
    }

    public float getMinBid() {
        return currentBid + auctionSettings.getMinIncrement();
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public AuctionStatus start(Player player, ItemStack itemStack, float startBid)  {
        if(isRunning()) return AuctionStatus.RUNNING;
        if(isInCooldown()) return AuctionStatus.COOLDOWN;
        if(startBid < auctionSettings.getMinStart()) return AuctionStatus.UNDER_MIN;
        if(startBid > auctionSettings.getMaxStart()) return AuctionStatus.OVER_MAX;

        currentBid = startBid;
        this.itemStack = itemStack;
        owner = player;
        currentWinner = owner;
        isRunning = true;
        timeRemaining = auctionSettings.getDefaultTime();
        Bukkit.getServer().getPluginManager().callEvent(new AuctionEvent(this, AuctionEvent.EventType.START));
        timerID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, timerRunnable, TICKS_PER_SECOND, TICKS_PER_SECOND);
        return AuctionStatus.SUCCESS;
    }

    public int getTimerID() {
        return timerID;
    }

    public AuctionStatus bid(Player player, long bid) {
        if(!isRunning) return AuctionStatus.NOT_RUNNING;
        if(currentBid + bid > bid + auctionSettings.getMaxIncrement()) return AuctionStatus.OVER_MAX;
        if(currentBid + bid < bid + auctionSettings.getMinIncrement()) return AuctionStatus.UNDER_MIN;
        if(currentWinner != null && currentWinner == player) return AuctionStatus.WINNING;

        currentWinner = player;
        currentBid = bid;
        return AuctionStatus.SUCCESS;
    }

    public void end() {
        if(isRunning()) {
            Bukkit.getServer().getPluginManager().callEvent(new AuctionEvent(this, AuctionEvent.EventType.END));
            isRunning = false;
            inCooldown = true;

            plugin.getServer().getScheduler().cancelTask(timerID);
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,cooldownRunnable, auctionSettings.getCooldown()*TICKS_PER_SECOND);
        }
    }

    public void cancel() {
        Bukkit.getServer().getPluginManager().callEvent(new AuctionEvent(this, AuctionEvent.EventType.CANCEL));
        isRunning = false;
        inCooldown = true;
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, cooldownRunnable, auctionSettings.getCooldown()*TICKS_PER_SECOND);
    }

    public String infoReplace(String info) {
        info = info.replace("%owner%", owner.getName());
        info = info.replace("%quantity%", String.valueOf(itemStack.getAmount()));
        info = info.replace("%item%", itemStack.getType().name());
        info = info.replace("%bid%", String.format("%,.2f", currentBid));
        if(currentWinner != null) info = info.replace("%winner%", currentWinner.getName());
        else info = info.replace("%winner%", "None");
        return info;
    }

    public long getTimeRemaining() {
        return timeRemaining;
    }

    public void setTimeRemaining(long time) {
        timeRemaining = time;
    }

    private void callTimerEvent() {
        Bukkit.getServer().getPluginManager().callEvent(new AuctionEvent(this, AuctionEvent.EventType.TIMER));
    }

    public Salesmania getPlugin() {
        return plugin;
    }
}
