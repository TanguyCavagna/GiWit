package com.toguy.giwit.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class PVPEvent implements Listener {

	/**
	 * Desactive le pvp si le pvp n'est pas activer
	 * 
	 * @param e
	 */
	@EventHandler
	public void onPlayerAttack(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			e.setCancelled(true);
		}
	}
	
}
