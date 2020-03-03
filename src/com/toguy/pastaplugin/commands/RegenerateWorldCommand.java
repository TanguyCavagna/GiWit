package com.toguy.pastaplugin.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RegenerateWorldCommand implements CommandExecutor {
	private World world;
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			if (((Player)sender).isOp()) {
				createWorld();
				
				for (Player player : Bukkit.getOnlinePlayers())
					player.teleport(world.getSpawnLocation());
			}
		}
		
		return true;
	}
	
	/**
	 * Créer un nouveau monde à chaque lancement de serveur
	 */
	private void createWorld() {
		WorldCreator creator = new WorldCreator("UHC-" + UUID.randomUUID().toString().split("-")[0]);
		creator.generateStructures(true);
		world = creator.createWorld();
	}
}
