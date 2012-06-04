package net.invisioncraft.plugins.salesmania.util;

import net.invisioncraft.plugins.salesmania.Salesmania;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Owner: Justin
 * Date: 6/3/12
 * Time: 2:52 AM
 */
public class ItemManager {
    private Salesmania plugin;
    private static Logger consoleLogger = Salesmania.consoleLogger;
    public ItemManager(Salesmania plugin) {
        this.plugin = plugin;
    }

    public static int getQuantity(Player player, ItemStack itemStack) {
        int quantity = 0;
        for(Map.Entry<Integer, ? extends ItemStack> entry : player.getInventory().all(itemStack.getTypeId()).entrySet()) {
            // Check for data value + enchants
            if(!entry.getValue().getEnchantments().equals(itemStack.getEnchantments())) continue;
            if(!entry.getValue().getData().equals(itemStack.getData())) continue;
            quantity += entry.getValue().getAmount();
        }
        return quantity;
    }

    public static boolean takeItem(Player player, ItemStack itemStack) {
        int remainingQuantity = itemStack.getAmount();
        for(Map.Entry<Integer, ? extends ItemStack> entry : player.getInventory().all(itemStack.getTypeId()).entrySet()) {

            // Check for data value + enchants
            if(!entry.getValue().getEnchantments().equals(itemStack.getEnchantments())) continue;
            if(!entry.getValue().getData().equals(itemStack.getData())) continue;

            if(remainingQuantity == 0) break;
            ItemStack stack = entry.getValue();

            if(remainingQuantity >= stack.getAmount()) {
                remainingQuantity -= stack.getAmount();
                player.getInventory().removeItem(stack);
            }

            else if (remainingQuantity < stack.getAmount()) {
                stack.setAmount(stack.getAmount()-remainingQuantity);
                remainingQuantity -= remainingQuantity;
            }
        }
        if(remainingQuantity != 0) {
            consoleLogger.severe("Could not take expected quantity!");
            return false;
        }
        else return true;
    }
}
