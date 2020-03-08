package com.toguy.giwit;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.util.Arrays;

import com.toguy.giwit.commands.SwapCommand;
import com.toguy.giwit.commands.TwitchCommand;
import com.toguy.giwit.commands.UHCAdministrationCommand;
import com.toguy.giwit.commands.tab_completion.SwapTabCompletion;
import com.toguy.giwit.commands.tab_completion.TwitchTabCompletion;
import com.toguy.giwit.commands.tab_completion.UHCAdministrationTabCompletion;
import com.toguy.giwit.events.UHCEvent;
import com.toguy.giwit.scoreboards.uhc.TeamScoreboards;
import com.toguy.giwit.scoreboards.uhc.UHCTeam;

import net.md_5.bungee.api.ChatColor;

/**
 * Plugin permettant de gérer un uhc, lié un compte twitch à un joueur ainsi qu'administrer un serveur
 * 
 * @author Tanguy Cavagna
 * @version 0.1
 */
public class GiWit extends JavaPlugin implements Listener {
	
	// Public
	//=============================
	/**
	 * Méthode appelée lors de l'activation du plugin
	 */
	public void onEnable() {
		try {
			this.saveDefaultConfig();
		} catch (Exception e) {
			this.getConfig().options().copyDefaults();
		}
		
		// Debug message
		Bukkit.getServer().getLogger().info("GiWit plugin is enable");
		
		TeamScoreboards.purgeScoreboards();
		
		// Setup des commandes
		UHCAdministrationCommand uhcAdministrationCommand = new UHCAdministrationCommand();
		getCommand("uhc").setExecutor(uhcAdministrationCommand);
		getCommand("swap").setExecutor(new SwapCommand());
		getCommand("twitch").setExecutor(new TwitchCommand());
		getCommand("uhc").setTabCompleter(new UHCAdministrationTabCompletion());
		getCommand("swap").setTabCompleter(new SwapTabCompletion());
		getCommand("twitch").setTabCompleter(new TwitchTabCompletion());
		
		// Setup de events
		Bukkit.getServer().getPluginManager().registerEvents(uhcAdministrationCommand, this);
		Bukkit.getServer().getPluginManager().registerEvents(new UHCEvent(), this);
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		
		// construit les equipes de base
		for (Player player : Bukkit.getOnlinePlayers())
			this.setupPlayerInfos(player);
		
		TeamScoreboards.getInstance().getTeams(); // Pour mettre a jour la liste des équipes
	}
	
	/**
	 * Méthode appelée lors de la désactivation du plugin
	 */
	public void onDisable() {
		Bukkit.getServer().getLogger().info("GiWit plugin is disable");
	}
	
	/**
	 * Méthode appelée lors de l'envoie d'une commande via le chat
	 * 
	 * @param sender Entité qui envoie la commande
	 * @param cmd Commande envoyée
	 * @param commandLabel ????
	 * @param args Arguments suivants la commande
	 */
	public boolean onCommand(CommandSender sender, Command cmd, String commandlabel, String[] args) {
		// Affiche un message global du type :
		//
		// ---------------------------------------
		//
		// Mon message
		//
		// ---------------------------------------
		if (cmd.getName().equalsIgnoreCase("warn")) {
			if (sender instanceof Player) {
				Player p = (Player)sender;
				
				if (p.isOp()) {
					if (!args[0].isEmpty()) {
						Bukkit.getServer().broadcastMessage("");
						Bukkit.getServer().broadcastMessage(ChatColor.DARK_RED + "-------------------------------------------------");
						Bukkit.getServer().broadcastMessage("");
						Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + String.join(" ", args));
						Bukkit.getServer().broadcastMessage("");
						Bukkit.getServer().broadcastMessage(ChatColor.DARK_RED + "-------------------------------------------------");
						Bukkit.getServer().broadcastMessage("");
					}
				} else {
					// Do nothing
				}
			}
		}
		
		// Affiche un message global au centre de l'écran des joueur
		// 
		// Le premier argument fait office de titre principale, le reste fait office de sous-titre
		if (cmd.getName().equalsIgnoreCase("alert")) {
			if (sender instanceof Player) {
				Player p = (Player)sender;
				
				if (p.isOp()) {
					if (!args[0].isEmpty()) {
						for (Player player : Bukkit.getOnlinePlayers())
							player.sendTitle(args[0], String.join(" ", Arrays.copyOfRange(args, 1, args.length)), 1, 20, 1);
					}
				} else {
					// Do nothing
				}
			}
		}
		
		// Affiche l'aide des commandes
		if (cmd.getName().equalsIgnoreCase("help")) {
			if (sender instanceof Player) {
				Player p = (Player)sender;
				
				if (p.isOp()) {
					p.sendMessage("");
					p.sendMessage(ChatColor.GOLD + "----------" + ChatColor.WHITE + " Aide " + ChatColor.GOLD + "----------");
					p.performCommand("uhc");
					p.performCommand("twitch");
					p.performCommand("swap");
				} else {
					// Do nothing
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Evènement appelé lorsqu'un joueur écrit dans le chat
	 * 
	 * @param e Evènement de chat
	 */
	@EventHandler
	public void onPlayerSay(AsyncPlayerChatEvent e) {
		Player player = e.getPlayer();
		String message = e.getMessage();
		Boolean chatPrefixEnable = this.getConfig().getBoolean("chat-prefix.enable");
		String teamMessagePrefix = this.getConfig().getString("chat-prefix.team-prefix");
		String globalMessagePrefix = this.getConfig().getString("chat-prefix.global-prefix");
		
		// Parcoure toutes les équipes de l'uhc pour mettre la couleur correspondante dans le chat
		for (UHCTeam uhcTeam : TeamScoreboards.getInstance().getTeams().values()) {
			Team t = uhcTeam.getTeam();
			
			if (t.hasEntry(player.getName())) {
				e.setFormat(uhcTeam.getColor() + player.getDisplayName() + ChatColor.WHITE + ": " + message);
				
				if (chatPrefixEnable)
					e = updateMessageDestinationFromPrefixes(e, message, globalMessagePrefix, teamMessagePrefix, t);
				
				return;
			}
		}
		
		// Si aucun équipe n'a été trouvée pour le joueur, mettre un format par défaut
		e.setFormat(ChatColor.WHITE + player.getDisplayName() + ": " + message);
	}
	
	/**
	 * Evènement appelé lors du join d'un joueur 
	 * 
	 * @param e Evènement pour le join d'un joueur 
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		
		this.setupPlayerInfos(player);
	}

	// Private
	//=============================
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

	/**
	 * Attribut les scoreboards a un joueur
	 * 
	 * @param player Joueur a setup
	 */
	private void setupPlayerInfos(Player player) {
		player.setGameMode(GameMode.ADVENTURE);
		player.setDisplayName(player.getName());
		player.setPlayerListName(player.getName());
		
		if (player.isOp()) {
			TeamScoreboards.getInstance().addPlayerToAdminTeam(player);
			player.setGameMode(GameMode.CREATIVE);
		}

		player.setScoreboard(TeamScoreboards.getInstance().getScoreboard());
		player.getInventory().setItem(0, new ItemStack(Material.COMPASS));
	}
}