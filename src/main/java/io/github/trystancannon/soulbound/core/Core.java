/*
 * The MIT License
 *
 * Copyright 2015 Trystan Cannon (tccannon@live.com).
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.github.trystancannon.soulbound.core;

import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class for the Soulbound plugin.
 * 
 * Creates "soul bound" items by setting the first line of their
 * lore to "Soulbound".
 * 
 * Soulbound items are destroyed upon death and upon being dropped
 * into the world. This is accomplished by listening to the
 * following events:
 *      - PlayerDropEvent -- Remove soulbound drops when a player drops them.
 *      - PlayerDeathEvent -- Remove soulbound drops that occur at death and
 *                            remove soulbound items remaining in the player's inventory.
 * 
 * Commands:
 *      - /soulbound -- Binds the item in the player's hand.
 * 
 * Permissions:
 *      - soulbound.cando -- Allows for the usage of /soulbound.
 * 
 * @author Trystan Cannon (tccannon@live.com)
 */
public final class Core extends JavaPlugin implements Listener {
    
    /**
     * Registers the plugin for events.
     */
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }
    
    /**
     * Removes soulbound items from the world when they are dropped.
     * 
     * @param drop
     *          The drop event.
     */
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent drop) {
        if (isSoulbound(drop.getItemDrop().getItemStack())) {
            drop.getItemDrop().remove();
            drop.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "Destroyed that soulbound item!");
        }
    }
    
    /**
     * Removes any soulbound items from the player's inventory upon death.
     * 
     * @param death
     *          The death event.
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent death) {
        int itemsDestroyed = 0;
        
        if (death.getKeepInventory()) {
        // Remove soulbound items from the deceased player's inventory.
            for (ItemStack stack : death.getEntity().getInventory().getContents()) {
                if (isSoulbound(stack)) {
                    stack.setType(Material.AIR);
                    itemsDestroyed++;
                }
            }
        } else {
            // Remove soulbound drops.
            for (ItemStack stack : death.getDrops()) {
                if (isSoulbound(stack)) {
                    stack.setType(Material.AIR);
                    itemsDestroyed++;
                }
            }
        }
        
        death.getEntity().sendMessage(ChatColor.LIGHT_PURPLE + "Destroyed " + itemsDestroyed + " soulbound items!");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (sender instanceof Player && sender.hasPermission("soulbound.cando")) {
            Player player = (Player) sender;
            
            // Make sure the item in hand is not air and is not stackable. This is
            // a check for if the item is not a block.
            if (player.getItemInHand().getType() != Material.AIR && player.getItemInHand().getMaxStackSize() == 1) {
                ItemStack item = player.getItemInHand();
                
                // Make sure the item is not already soulbound.
                if (!isSoulbound(item)) {
                    soulBind(item);
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "Soulbound that item!");
                } else {
                    player.sendMessage(ChatColor.RED + "That item is already soulbound.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "Cannot soul bind that item.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You must be a player to use this command.");
        }
        
        return true;
    }
    
    /**
     * Soul binds the given item by setting the first line of its
     * lore to: Soulbound.
     * @param item 
     */
    private static void soulBind(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = Arrays.asList("Soulbound");
        
        if (meta.hasLore()) {
            // Add a break between the soulbound lore and
            // the old lore.
            lore.add("");
            
            for (String line : meta.getLore()) {
                lore.add(line);
            }
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
    }
    
    /**
     * @param item
     *          The item to check.
     * @return
     *          <code>true</code> if the given item is soul bound.
     */
    private static boolean isSoulbound(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return false;
        }
        
        // The first line should be: Soulbound
        return item.getItemMeta().getLore().get(0).equals("Soulbound");
    }
    
}