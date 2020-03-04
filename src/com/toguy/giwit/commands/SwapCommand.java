package com.toguy.giwit.commands;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SwapCommand implements CommandExecutor {
	
	static Random rd = new Random();
	static int playerPerTeam = 0;
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if (sender instanceof Player) {
			// Swap les joueurs entre eux
			if (args[0].equalsIgnoreCase("players")) {
				if (playerPerTeam == 0) {
					sender.sendMessage("Fait la commande /swap set <nombreDeJoueurParTeam> avant de swap les joueurs !!");
					return true;
				}
				
				try {
					// Diviser par deux car on swap a chaque fois deux joueurs entre eux
					int swapCount = (Bukkit.getOnlinePlayers().size() / playerPerTeam) / 2;
					
					for (int i = 0; i < swapCount; i++) {
						Player p1 = (Player)Bukkit.getOnlinePlayers().toArray()[rd.nextInt(Bukkit.getOnlinePlayers().size())];
						Player p2 = null;
						
						// Obligatoire pour ne pas piocher deux fois le meme joueur
						do {
							p2 = (Player)Bukkit.getOnlinePlayers().toArray()[rd.nextInt(Bukkit.getOnlinePlayers().size())];
						} while (p2.getUniqueId() == p1.getUniqueId());
						
						Location loc = p1.getLocation().clone();
												
						p1.teleport(p2.getLocation());
						p2.teleport(loc);
				}
				} catch (Exception e) {
					sender.sendMessage(e.getMessage());
				}
			}
			
			// Swap les inventaires entre joeurs
			if (args[0].equalsIgnoreCase("inventory")) {
				if (playerPerTeam == 0) {
					sender.sendMessage("Fait la commande /swap set <nombreDeJoueurParTeam> avant de swap les joueurs !!");
					return true;
				}
				
				try {
					// Diviser par deux car on swap a chaque fois deux joueurs entre eux
					int swapCount = (Bukkit.getOnlinePlayers().size() / playerPerTeam) / 2;
					HashMap<UUID, ItemStack[]> items = new HashMap<UUID, ItemStack[]>();
					HashMap<UUID, ItemStack[]> armor = new HashMap<UUID, ItemStack[]>();
					HashMap<UUID, ItemStack[]> extra = new HashMap<UUID, ItemStack[]>();
					
					for (int i = 0; i < swapCount; i++) {
						Player p1 = (Player)Bukkit.getOnlinePlayers().toArray()[rd.nextInt(Bukkit.getOnlinePlayers().size())];
						Player p2 = null;
						
						// Obligatoire pour ne pas piocher deux fois le meme joueur
						do {
							p2 = (Player)Bukkit.getOnlinePlayers().toArray()[rd.nextInt(Bukkit.getOnlinePlayers().size())];
						} while (p2.getUniqueId() == p1.getUniqueId());
						
						// Store p1
						items.put(p1.getUniqueId(), p1.getInventory().getContents());
						armor.put(p1.getUniqueId(), p1.getInventory().getArmorContents());
						extra.put(p1.getUniqueId(), p1.getInventory().getExtraContents());
						
						// Store p2
						items.put(p2.getUniqueId(), p2.getInventory().getContents());
						armor.put(p2.getUniqueId(), p2.getInventory().getArmorContents());
						extra.put(p2.getUniqueId(), p2.getInventory().getExtraContents());
						
						// Restore inventories
						p1.getInventory().setContents(items.get(p2.getUniqueId()));
						p1.getInventory().setArmorContents(armor.get(p2.getUniqueId()));
						p1.getInventory().setExtraContents(extra.get(p2.getUniqueId()));
						
						p2.getInventory().setContents(items.get(p1.getUniqueId()));
						p2.getInventory().setArmorContents(armor.get(p1.getUniqueId()));
						p2.getInventory().setExtraContents(extra.get(p1.getUniqueId()));
					}
				} catch (Exception e) {
					sender.sendMessage(e.getMessage());
				}
			}
			
			// Set le nombre de joueur par team pour savoir combien de joueurs il y a a swap
			if (args[0].equalsIgnoreCase("set")) {
				try {
					playerPerTeam = Integer.parseInt(args[1]);
				} catch (Exception e) {
					Bukkit.broadcastMessage("Le numero entrée est faux");
				}
			}
		}
		
		return true;
	}
}
