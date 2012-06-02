package net.invisioncraft.plugins.salesmania.commands.salesmania;

import net.invisioncraft.plugins.salesmania.Salesmania;
import net.invisioncraft.plugins.salesmania.configuration.Locale;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Owner: Byte 2 O Software LLC
 * Date: 5/23/12
 * Time: 8:08 PM
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
public class SalesmaniaCommandExecutor implements CommandExecutor {
    protected Salesmania plugin;
    protected LocaleCommand localeCommand;

    enum SalesmaniaCommand {
        LOCALE,
        RELOAD
    }

    public SalesmaniaCommandExecutor(Salesmania plugin) {
        this.plugin = plugin;
        localeCommand = new LocaleCommand(plugin);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Locale locale = plugin.getLocaleHandler().getLocale(sender);
        SalesmaniaCommand salesmaniaCommand;

        // Syntax
        if(args.length < 1) {
            sender.sendMessage(locale.getMessageList("Syntax.Salesmania.salesmania").toArray(new String[0]));
            return false;
        }
        try {
            salesmaniaCommand = SalesmaniaCommand.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException ex) {
            sender.sendMessage(locale.getMessageList("Syntax.Salesmania.salesmania").toArray(new String[0]));
            return false;
        }

        switch(salesmaniaCommand) {
            case LOCALE:
                localeCommand.execute(sender, command, label, args);
                return true;
            case RELOAD:
                if(!sender.hasPermission("salesmania.admin.reload")) return false;
                plugin.reloadConfig(sender);
                return true;
            default:
                return false;
        }
    }
}
