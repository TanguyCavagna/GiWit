package com.toguy.giwit.gui;

import java.util.ArrayList;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.toguy.giwit.scoreboards.uhc.TeamScoreboards;
import com.toguy.giwit.scoreboards.uhc.UHCTeam;

public class TeamSelector {

	// Constantes
	public static final String GUI_NAME = "S�lection des teams"; 
	
	// Public
	//=============================
	/**
	 * Cr�er l'inventaire pour la s�lection des teams
	 * 
	 * @param targetPlayer Joueur ayant cliqu�
	 * @return
	 */
	public static Inventory generateTeamSelectorInventory(Player targetPlayer) {
		Inventory gui = Bukkit.createInventory(targetPlayer, 27, GUI_NAME);
		
		ItemStack[] menu_items = new ItemStack[TeamScoreboards.getInstance().getTeams().size()];
		
		// R�cup�re toutes les �quipes et cr�er l'item correspondant pour le menu
		int index = 0;
		for (UHCTeam team : TeamScoreboards.getInstance().getTeams().values()) {
			Set<String> teamPlayers = TeamScoreboards.getInstance().getPlayerInTeam(team.getName());
			
			menu_items[index++] = createTeamMenuItem(team.getColor(), team.getName(), teamPlayers, team.getItem(), team.getMaxPlayers());
		}
		
		gui.setContents(menu_items);
		
		return gui;
	}
	
	// Private
	//=============================
	/**
	 * Cr�er un item pour l'�quipe
	 * 
	 * @param color Couleur de l'�quipe
	 * @param itemName Nom de l'item
	 * @param playerNames Nom des joueurs pr�sents dans l'�quipe
	 * @param itemMaterial Materiel � utilis� pour l'item
	 * @param maxPlayerCount Nombre de joueurs max
	 * @return
	 */
	private static ItemStack createTeamMenuItem(ChatColor color, String itemName, Set<String> playerNames, Material itemMaterial, int maxPlayerCount) {
		int freeTeamEmplacements = maxPlayerCount - playerNames.size();
		
		ItemStack team = new ItemStack(itemMaterial);
		ItemMeta teamMeta = team.getItemMeta();
		teamMeta.setDisplayName(color + itemName);
		
		// Ajout d'un effet d'enchant si il n'y a plus de places
		if (freeTeamEmplacements == 0)
			teamMeta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
		teamMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		
		// Ajout du Lore
		ArrayList<String> teamLore = new ArrayList<String>();
		
		teamLore.add(ChatColor.GRAY + "Membres de l'�quipe:");
		teamLore.add("");
		
		for (String playerName : playerNames)
			teamLore.add(ChatColor.YELLOW + "# " + ChatColor.AQUA +  playerName);
		
		for (int i = 0; i < freeTeamEmplacements; i++)
			teamLore.add(ChatColor.YELLOW + "# " + ChatColor.DARK_GRAY +  "[Emplacement libre]");
		
		teamLore.add("");
		teamLore.add(ChatColor.GOLD + ">" + ChatColor.GREEN + " Clique pour rejoindre");
		
		// Mise a jour de l'item
		teamMeta.setLore(teamLore);
		team.setItemMeta(teamMeta);
		
		return team;
	}
	
}
