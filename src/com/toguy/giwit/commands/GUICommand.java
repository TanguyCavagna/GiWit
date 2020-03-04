package com.toguy.giwit.commands;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GUICommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if (sender instanceof Player) {
			Player player = (Player)sender;
			
			Inventory gui = Bukkit.createInventory(player, 9, ChatColor.GOLD + "Custom GUI");
			
			ItemStack glass_pane = new ItemStack(Material.GLASS_PANE);
			ItemMeta glass_pane_meta = glass_pane.getItemMeta();
			glass_pane_meta.setDisplayName(" ");
			glass_pane.setItemMeta(glass_pane_meta);
			
			ItemStack start = new ItemStack(Material.EMERALD_BLOCK);
			ItemMeta start_meta = start.getItemMeta();
			start_meta.setDisplayName(ChatColor.GREEN + "Start new game");
			ArrayList<String> stat_lore = new ArrayList<String>();
			stat_lore.add(ChatColor.GOLD + "Start a new UHC game");
			start_meta.setLore(stat_lore);
			start.setItemMeta(start_meta);
			
			ItemStack[] menu_items = { glass_pane, glass_pane, start, glass_pane, start, glass_pane, start, glass_pane, glass_pane };
			gui.setContents(menu_items);

			player.openInventory(gui);
		}
		
		return true;
	}
	
}
