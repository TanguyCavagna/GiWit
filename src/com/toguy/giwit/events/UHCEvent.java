package com.toguy.giwit.events;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Team;

import com.toguy.giwit.GiWit;
import com.toguy.giwit.gui.TeamSelector;
import com.toguy.giwit.scoreboards.uhc.TeamScoreboards;
import com.toguy.giwit.scoreboards.uhc.UHCTeam;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class UHCEvent implements Listener {
	
	// Public
	//=============================
	/**
	 * Ouvre le menu de séléction de teams
	 * 
	 * @param e
	 */
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
	
	/**
	 * Ajoute le joueurs dans la team cliqueé
	 * 
	 * @param e
	 */
	@EventHandler
	public void onTeamColorClick(InventoryClickEvent e) {
		Player player = (Player)e.getWhoClicked();
		
		if (e.getView().getTitle().equalsIgnoreCase(TeamSelector.GUI_NAME)) {
			if (e.getRawSlot() > 26 || e.getCurrentItem() == null) { // Max count of slot in TeamSelector GUI
				e.setCancelled(true);
				return;
			}
			
			String clickedItemName = e.getCurrentItem().getItemMeta().getDisplayName();
			player.closeInventory();
			player.setScoreboard(TeamScoreboards.getInstance().addPlayerInTeam(player, clickedItemName));
			
			ItemStack clickedTeamItem = e.getCurrentItem();
			ItemMeta clickedTeamItemMeta = clickedTeamItem.getItemMeta();
			ArrayList<String> clickedTeamItemLore = new ArrayList<String>();
			
			Team t = TeamScoreboards.getInstance().getTeams().get(ChatColor.stripColor(clickedItemName)).getTeam();
			clickedTeamItemMeta.setDisplayName(ChatColor.ITALIC + "" + ChatColor.WHITE + "Equipe " + t.getColor() + t.getDisplayName());
			clickedTeamItemLore.add("");
			clickedTeamItemMeta.setLore(clickedTeamItemLore);
			clickedTeamItem.setItemMeta(clickedTeamItemMeta);
			
			player.getInventory().setItem(4, new ItemStack(clickedTeamItem));
			player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_AQUA + "Tu as rejoins les " + t.getColor() + t.getDisplayName() + ChatColor.DARK_AQUA + " !"));
			
			e.setCancelled(true);
		}
	}
	
	/**
	 * Evènement appelé lorsqu'un joueur écrit dans le chat
	 * 
	 * @param e Evènement de chat
	 */
	@EventHandler
	public void onPlayerSay(AsyncPlayerChatEvent e) {
		Plugin plugin = GiWit.getPlugin(GiWit.class);
		
		Player player = e.getPlayer();
		String message = e.getMessage();
		Boolean chatPrefixEnable = plugin.getConfig().getBoolean("chat-prefix.enable");
		String teamMessagePrefix = plugin.getConfig().getString("chat-prefix.team-prefix");
		String globalMessagePrefix = plugin.getConfig().getString("chat-prefix.global-prefix");
		
		// Parcoure toutes les équipes de l'uhc pour mettre la couleur correspondante dans le chat
		for (UHCTeam uhcTeam : TeamScoreboards.getInstance().getTeams().values()) {
			Team t = uhcTeam.getTeam();
			
			if (t.hasEntry(player.getName())) {
				e.setFormat(uhcTeam.getColor() + player.getDisplayName() + ChatColor.DARK_GRAY + " » " + ChatColor.WHITE + message);
				
				if (chatPrefixEnable)
					e = updateMessageDestinationFromPrefixes(e, message, globalMessagePrefix, teamMessagePrefix, t);
				
				return;
			}
		}
		
		// Si aucun équipe n'a été trouvée pour le joueur, mettre un format par défaut
		e.setFormat(ChatColor.GRAY + player.getDisplayName() + ChatColor.DARK_GRAY + " » " + ChatColor.WHITE + message);
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Block block = e.getBlock();
		Player player = e.getPlayer();
		
		if (player.getGameMode() == GameMode.SURVIVAL) {
			// Cut clean blocks
			switch (e.getBlock().getType()) {
				case IRON_ORE:
					e.setCancelled(true);
					block.setType(Material.AIR);
					block.getWorld().dropItem(block.getLocation(), new ItemStack(Material.IRON_INGOT));
					player.giveExp(3);
					break;
					
				case GOLD_ORE:
					e.setCancelled(true);
					block.setType(Material.AIR);
					block.getWorld().dropItem(block.getLocation(), new ItemStack(Material.GOLD_INGOT));
					player.giveExp(3);
					break;
					
				case SAND:
					e.setCancelled(true);
					block.setType(Material.AIR);
					block.getWorld().dropItem(block.getLocation(), new ItemStack(Material.GLASS));
					break;
		
				default:
					break;
			}
		}
	}
	
	/**
	 * Modifie la cible du message en fonction du préfix du message
	 * @param e Evenement de chat
	 * @param message Message envoyer
	 * @param globalMessagePrefix Prefix du global
	 * @param teamMessagePrefix Prefix de la team
	 * @param t Team dans laquelle le joueur se trouve
	 * @return
	 */
	private AsyncPlayerChatEvent updateMessageDestinationFromPrefixes(AsyncPlayerChatEvent e, String message, String globalMessagePrefix, String teamMessagePrefix, Team t) {
		//========================================
		// ETANT DONNER QUE LE JOUEUR EST DANS UNE EQUIPE, IL FAUT QU'IL COMMUNIQUE DE FACON PRECISE
		//========================================
		// Envoie le message a tout le monde
		e.setCancelled(true);
		String format = e.getFormat();

		// Ne fait rien de special
		if (message.startsWith(globalMessagePrefix)) {
			format = this.removeCharAtPos(format, format.indexOf(globalMessagePrefix));
		    
			e.setFormat(format);
			e.setCancelled(false);
		} 
		// Envoie le message uniquement à ceux de la meme équipe
		else if (message.startsWith(teamMessagePrefix)) {
			format = this.removeCharAtPos(format, format.indexOf(teamMessagePrefix));
		    
			for (String playerName : t.getEntries()) {
				Player teamMate = Bukkit.getServer().getPlayer(playerName);
				teamMate.sendMessage(format);
			}
		} 
		
		return e;
	}
	
	/**
	 * Supprime un caractère a une position donnée
	 * 
	 * @param foo Chaine dans laquelle supprimer le caractère
	 * @param charPos Index du caractère
	 * @return
	 */
	private String removeCharAtPos(String foo, int charPos) {
		if (charPos > 0)
	    	return new StringBuilder(foo).deleteCharAt(charPos).toString();
		else
			return foo;
	}

}
