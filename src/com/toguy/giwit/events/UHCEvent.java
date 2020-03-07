package com.toguy.giwit.events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.toguy.giwit.gui.uhc.TeamSelector;
import com.toguy.giwit.scoreboards.uhc.TeamScoreboards;

public class UHCEvent implements Listener {
	
	@EventHandler
	public void onCompassClick(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		Action action = e.getAction();
		ItemStack item = e.getItem();
		
		if (action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)) {
			if (item != null && item.getType() == Material.COMPASS) {
				player.openInventory(TeamSelector.generateTeamSelectorInventory(player));
			}
		}
	}
	
	@EventHandler
	public void onTeamColorClick(InventoryClickEvent e) {
		Player player = (Player)e.getWhoClicked();
		String clickedItemName = e.getCurrentItem().getItemMeta().getDisplayName();
		
		if (e.getView().getTitle().equalsIgnoreCase(TeamSelector.GUI_NAME)) {
			player.closeInventory();
			player.setScoreboard(TeamScoreboards.getInstance().addPlayerInTeam(player, clickedItemName));
			
			e.setCancelled(true);
		}
	}
	
}
