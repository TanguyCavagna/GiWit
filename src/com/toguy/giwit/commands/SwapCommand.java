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

import net.md_5.bungee.api.ChatColor;

public class SwapCommand implements CommandExecutor {
	
	// Champs
	static Random rd = new Random();
	static int playerPerTeam = 0;
	
	/**
	 * Swap les joueurs ou les inventaires entre joueurs
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if (sender instanceof Player) {
			if (args.length <= 0) {
				Player player = (Player)sender;
				player.sendMessage("");
				player.sendMessage(this.alternateColorForString(ChatColor.GRAY, ChatColor.WHITE, "☰☰☰☰☰☰☰☰") + ChatColor.AQUA + " /swap" + ChatColor.RED + " [OP] " + this.alternateColorForString(ChatColor.GRAY, ChatColor.WHITE, "☰☰☰☰☰☰☰☰"));
				player.sendMessage(ChatColor.DARK_GRAY + "> " + ChatColor.AQUA + "players: " + ChatColor.WHITE + "Echange la position entre les joueurs");
				player.sendMessage(ChatColor.DARK_GRAY + "> " + ChatColor.AQUA + "inventory: " + ChatColor.WHITE + "Echange l'inventaire entre joueurs");
				player.sendMessage(ChatColor.DARK_GRAY + "> " + ChatColor.AQUA + "set <player_per_team>: " + ChatColor.WHITE + "Met a jour le nombre de joueur a swap par équipe");
				
				return true;
			}
			
			// Swap les joueurs entre eux
			if (args[0].equalsIgnoreCase("players")) {
				if (playerPerTeam == 0) {
					sender.sendMessage(ChatColor.RED + "> " + ChatColor.GOLD + "Fait la commande /swap set <player_per_team> avant de swap les joueurs !!");
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
					sender.sendMessage(ChatColor.RED + "> " + ChatColor.GOLD + "Fait la commande /swap set <player_per_team> avant de swap les joueurs !!");
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
					playerPerTeam = Integer.parseInt(args[1]) > 0 ? Integer.parseInt(args[1]) : 1;
				} catch (Exception e) {
					Bukkit.broadcastMessage("Le numero entrée n'est pas valide");
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Retourne une chaine de caractère avec des couleurs en altérnance
	 * @return
	 */
	private String alternateColorForString(ChatColor color1, ChatColor color2, String foo) {
		String result = "";
		
		for (int i = 0; i < foo.length(); i++) {
			if (i % 2 == 0) {
				result += color1 + String.valueOf(foo.charAt(i));
			} else {
				result += color2 + String.valueOf(foo.charAt(i));
			}
		}
		
		return result;
	}

}
