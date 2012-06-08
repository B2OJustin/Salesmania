package net.invisioncraft.plugins.salesmania.commands.auction;

import net.invisioncraft.plugins.salesmania.Auction;
import net.invisioncraft.plugins.salesmania.CommandHandler;
import net.invisioncraft.plugins.salesmania.Salesmania;
import net.invisioncraft.plugins.salesmania.configuration.AuctionSettings;
import net.invisioncraft.plugins.salesmania.configuration.Locale;
import net.invisioncraft.plugins.salesmania.util.ItemManager;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Owner: Byte 2 O Software LLC
 * Date: 5/17/12
 * Time: 10:25 AM
 */
/*
Copyright 2012 Byte 2 O Software LLC
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

public class AuctionStart extends CommandHandler {
    AuctionSettings auctionSettings;
    public AuctionStart(Salesmania plugin) {
        super(plugin);
        auctionSettings = plugin.getSettings().getAuctionSettings();
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        Locale locale = plugin.getLocaleHandler().getLocale(sender);

        // Console check
        if(!(sender instanceof Player)) {
            sender.sendMessage(locale.getMessage("Console.cantStartAuction"));
            return false;
        }

        Player player = (Player) sender;
        Auction auction = plugin.getAuction();
        ItemStack itemStack = player.getItemInHand().clone();

        // Disable check
        if(!auctionSettings.getEnabled()) {
            sender.sendMessage(locale.getMessage("Auction.disabled"));
            return false;
        }

        // Creative check
        if(!auctionSettings.getAllowCreative() && player.getGameMode() == GameMode.CREATIVE) {
            sender.sendMessage(locale.getMessage("Auction.noCreative"));
            return false;
        }

        // Syntax check
        float startingBid;
        int quantity = 0;
        if(args.length < 2) {
            sender.sendMessage(locale.getMessage("Syntax.Auction.auctionStart"));
            return false;
        }
        try {
            startingBid = Float.valueOf(args[1]);
            if(args.length == 3) quantity = Integer.valueOf(args[2]);
        } catch (NumberFormatException ex) {
            sender.sendMessage(locale.getMessage("Syntax.Auction.auctionStart"));
            return false;
        }

        // Permission check
        if(!sender.hasPermission("salesmania.auction.start")) {
            sender.sendMessage(String.format(
                    locale.getMessage("Permission.noPermission"),
                    locale.getMessage("Permisson.Auction.start")));
            return false;
        }

        // Blacklist check
        if(auctionSettings.isBlacklisted(itemStack)) {
            player.sendMessage(locale.getMessage("Auction.itemBlacklisted"));
            return false;
        }

        // Quantity check
        if(quantity != 0) {
            if(quantity > ItemManager.getQuantity(player, itemStack)) {
                player.sendMessage(locale.getMessage("Auction.notEnough"));
                return false;
            }
            else itemStack.setAmount(quantity);
        }

        switch(auction.start(player, itemStack, startingBid)) {
            case RUNNING:
                player.sendMessage(locale.getMessage("Auction.alreadyStarted"));
                return false;
            case COOLDOWN:
                player.sendMessage(locale.getMessage("Auction.cooldown"));
                return false;
            case UNDER_MIN:
                player.sendMessage(String.format(locale.getMessage("Auction.startUnderMin"),
                        auctionSettings.getMinStart()));
                return false;
            case OVER_MAX:
                player.sendMessage(String.format(locale.getMessage("Auction.startOverMax"),
                        auctionSettings.getMaxStart()));
                return false;
            case SUCCESS:
                return true;
        }
        return false;
    }
}
