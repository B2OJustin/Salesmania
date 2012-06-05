package net.invisioncraft.plugins.salesmania.commands.auction;

import net.invisioncraft.plugins.salesmania.Auction;
import net.invisioncraft.plugins.salesmania.CommandHandler;
import net.invisioncraft.plugins.salesmania.Salesmania;
import net.invisioncraft.plugins.salesmania.configuration.Locale;
import net.milkbowl.vault.item.Items;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

public class AuctionBid extends CommandHandler {
    public AuctionBid(Salesmania plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        Locale locale = plugin.getLocaleHandler().getLocale(sender);
        if(!(sender instanceof Player)) {
            sender.sendMessage(locale.getMessage("Console.cantBid"));
        }
        Player player = (Player) sender;

        // Syntax check
        double bidAmount;
        if(args.length < 2) {
            sender.sendMessage(locale.getMessage("Syntax.Auction.auctionBid"));
            return false;
        }
        try {
            bidAmount = Double.valueOf(args[1]);
        }   catch (NumberFormatException ex) {
            sender.sendMessage(locale.getMessage("Syntax.Auction.auctionBid"));
            return false;
        }

        Auction auction = plugin.getAuction();

        if(!player.hasPermission("salesmania.auction.bid")) {
            player.sendMessage(String.format(
                    locale.getMessage("Permission.noPermission"),
                    locale.getMessage("Permisson.Auction.bid")));
            return false;
        }
        switch(auction.bid(player, bidAmount)) {
            case SUCCESS:
                player.sendMessage(String.format(
                        locale.getMessage("Auction.Bidding.bidSuccess"),
                        bidAmount, Items.itemById(auction.getItemStack().getTypeId()).getName()));
                return true;
            case OVER_MAX:
                player.sendMessage(String.format(
                        locale.getMessage("Auction.Bidding.overMax"),
                        auction.getMaxBid()));
                return true;
            case UNDER_MIN:
                player.sendMessage(String.format(
                        locale.getMessage("Auction.Bidding.underMin"),
                        auction.getMinBid()));
                return false;
            case NOT_RUNNING:
                player.sendMessage(locale.getMessage("Auction.notRunning"));
                return false;
            case WINNING:
                player.sendMessage(locale.getMessage("Auction.Bidding.playerWinning"));
                return false;
            case OWNER:
                player.sendMessage(locale.getMessage("Auction.Bidding.playerOwner"));
                return false;
        }

        return false;
    }
}
