package net.invisioncraft.plugins.salesmania.commands.auction;

import net.invisioncraft.plugins.salesmania.CommandHandler;
import net.invisioncraft.plugins.salesmania.Salesmania;
import net.invisioncraft.plugins.salesmania.configuration.Locale;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Owner: Byte 2 O Software LLC
 * Date: 5/24/12
 * Time: 7:21 AM
 */
public class AuctionInfo extends CommandHandler {
    public AuctionInfo(Salesmania plugin) {
        super(plugin);
    }
    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        Locale locale = localeHandler.getLocale(sender);
        if(!plugin.getAuction().isRunning()) {
            sender.sendMessage(locale.getMessage("Auction.notRunning"));
            return false;
        }
        List<String> infoList = locale.getMessageList("Auction.info");
        infoList = plugin.getAuction().infoReplace(infoList);
        infoList = plugin.getAuction().enchantReplace(infoList,
                locale.getMessage("Auction.enchant"),
                locale.getMessage("Auction.enchantInfo"), locale);
        sender.sendMessage(infoList.toArray(new String[0]));
        return true;
    }
}
