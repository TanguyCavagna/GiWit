package com.toguy.pastaplugin.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ClickGuiEvent implements Listener {
	@EventHandler
	public void onGuiClick(InventoryClickEvent e) {
		Player player = (Player)e.getWhoClicked();
		
		if (e.getView().getTitle().equalsIgnoreCase(ChatColor.GOLD + "Custom GUI")) {
			switch (e.getCurrentItem().getType()) {
				case EMERALD_BLOCK:
					player.closeInventory();
					Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "Game begin !");
					break;
				default:
					// Do nothing
					break;
			}
			
			e.setCancelled(true);
		}
	}
}
