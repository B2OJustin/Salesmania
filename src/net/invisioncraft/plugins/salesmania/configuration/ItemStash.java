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

package net.invisioncraft.plugins.salesmania.configuration;

import net.invisioncraft.plugins.salesmania.Salesmania;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class ItemStash extends Configuration {
    public ItemStash(Salesmania plugin) {
        super(plugin, "itemStash.yml");
    }

    public void store(Player player, ItemStack itemStack) {
        ArrayList<ItemStack> stackList = (ArrayList<ItemStack>) config.get(player.getName());
        stackList.add(itemStack);
        config.set(player.getName(), stackList);
        save();
    }

    public ArrayList<ItemStack> collect(Player player) {
        ArrayList<ItemStack> stackList = (ArrayList<ItemStack>) config.get(player.getName());
        config.set(player.getName(), null);
        return stackList;
    }

    public boolean hasItems(Player player) {
        if(config.contains(player.getName())) return true;
        else return false;
    }
}