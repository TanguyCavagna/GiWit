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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;
import org.json.simple.*;
import org.json.simple.parser.*;

import java.util.Arrays;

import com.google.gson.Gson;
import com.toguy.giwit.commands.GUICommand;
import com.toguy.giwit.commands.SwapCommand;
import com.toguy.giwit.commands.UHCAdministrationCommand;
import com.toguy.giwit.events.ClickGuiEvent;
import com.toguy.giwit.events.UHCEvent;
import com.toguy.giwit.scoreboards.uhc.TeamScoreboards;
import com.toguy.giwit.scoreboards.uhc.UHCTeam;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Plugin permettant de gérer un uhc, lié un compte twitch à un joueur ainsi qu'administrer un serveur
 * 
 * @author Tanguy Cavagna
 * @version 0.1
 */
public class GiWit extends JavaPlugin implements Listener {
	
	private Scoreboard board;
	private Team streamers;
	private Team admins;
		
	private Twitch twitch;
	
	/**
	 * Méthode appelée lors de l'activation du plugin
	 */
	public void onEnable() {
		this.getConfig().options().copyDefaults();
		this.saveDefaultConfig();
		
		// Debug message
		Bukkit.getServer().getLogger().info("awdawdawd-Player plugin is enable");
		
		// Setup des scoreboards
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		this.board = manager.getNewScoreboard();
		
		this.streamers = board.registerNewTeam("Streamer");
		this.streamers.setColor(org.bukkit.ChatColor.WHITE);
		
		this.admins = board.registerNewTeam("Admin");
		this.admins.setSuffix(ChatColor.GOLD + " [ADMIN]");
		this.admins.setColor(org.bukkit.ChatColor.WHITE);
		
		TeamScoreboards.purgeScoreboards();
		
		// Setup des commandes
		getCommand("uhc").setExecutor(new UHCAdministrationCommand(this.board));
		getCommand("gui").setExecutor(new GUICommand());
		getCommand("swap").setExecutor(new SwapCommand());
		
		// Setup de events
		Bukkit.getServer().getPluginManager().registerEvents(new UHCEvent(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new ClickGuiEvent(), this);
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		
		this.twitch = new Twitch();
		
		// TODO : Update le nombre de viewers des streamers
		/*
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				Bukkit.broadcastMessage("awdawd");
			}
		}, 0L, 20L);
		*/
		
		// construit les equipes de base
		for (Player player : Bukkit.getOnlinePlayers())
			this.setupPlayerInfos(player);
		
		TeamScoreboards.getInstance().getTeams(); // Pour mettre a jour la liste des équipes
	}
	
	/**
	 * Méthode appelée lors de la désactivation du plugin
	 */
	public void onDisable() {
		Bukkit.getServer().getLogger().info("Player-Player plugin is disable");
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
		// Permet de lié le joueur avec un compte twitch et d'avoir des stats concernant le stream
		if (cmd.getName().equalsIgnoreCase("twitch")) {
			if (sender instanceof Player) {
				Player p = (Player)sender;
				
				if (args.length == 0) {
					p.sendMessage(ChatColor.BLUE + "Si tu veux te lier a une chaine, fait /twitch link <chaine>. Si tu veux dé lié, fait /twitch unlink");
					return true;
				}
				
				if (args[0].equalsIgnoreCase("link")) {
					if (!args[1].isEmpty()) {
						// TODO : Décommenter les lignes ce dessous pour réactiver la permission
						if (!p.isOp()) {
							try {
								String response = this.twitch.getStreamInfosByUserLogin(args[1]);

								JSONParser parser = new JSONParser();
								JSONObject json = (JSONObject)parser.parse(response);
								JSONArray data = (JSONArray)json.get("data");
								
								Gson g = new Gson();
								Twitch.Stream stream = g.fromJson(data.get(0).toString(), Twitch.Stream.class);
																
								if (stream.isLive()) {
									this.streamers.setSuffix(ChatColor.RED + " ⏺ LIVE " + ChatColor.LIGHT_PURPLE + "(" + stream.getViewerCount().toString() + ")");
									this.streamers.addEntry(p.getName());
									
									p.setScoreboard(this.board);
									
									this.twitch.addPlayerInPlayerTwichName(p.getName(), stream.getUserName());
									
									TextComponent mainMsg = new TextComponent("Le lien avec ");
									mainMsg.setColor(ChatColor.GREEN);
									TextComponent link = new TextComponent("twitch.tv/" + args[1]);
									link.setBold(true);
									link.setUnderlined(true);
									link.setColor(ChatColor.DARK_PURPLE);
									link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click me !").create()));
									link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://twitch.tv/" + args[1]));
									mainMsg.addExtra(link);
									mainMsg.addExtra(" à bien été établie !");
									
									p.spigot().sendMessage(mainMsg);
								} else {
									p.sendMessage(ChatColor.RED + "[Error]" + ChatColor.WHITE + "Le live de " + args[1] + " n'est pas on.");
								}
							} catch (Exception e) {
								p.sendMessage(ChatColor.DARK_RED + e.getStackTrace().toString());
							}
						} else {
							p.sendMessage("Tu es admin donc pas d'autre grade pour toi.");
						}
					}
				}
				
				// Retire le lien entre le joueur et le stream
				if (args[0].equalsIgnoreCase("unlink")) {
					if (this.streamers.hasEntry(p.getName())) {
						this.streamers.removeEntry(p.getName());
						
						this.twitch.removePlayerInPlayerTwitchname(p.getName());
						
						if (p.isOp()) {
							if (!this.admins.hasEntry(p.getName()))
								this.admins.addEntry(p.getName());
						}
						
						p.setScoreboard(this.board);
						
						p.sendMessage(ChatColor.GREEN + "Le lien à bien été supprimé !");
					}
				}
			}
		}
		
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
				if (this.streamers.getEntries().contains(player.getName()))
					e.setFormat(uhcTeam.getColor() + this.twitch.getPlayerInPlayerTwichName(player.getName()) + ChatColor.GRAY + " (" + player.getName() + ")" + ChatColor.RED + " [LIVE]" + ChatColor.WHITE + " : " + message);
				else
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
		if (message.startsWith(globalMessagePrefix)) {
			e.setCancelled(true);
			
			String format = e.getFormat();
			// Ne fait rien de special
			int charPos = format.indexOf(globalMessagePrefix);
		    if (charPos > 0)
		    	format = new StringBuilder(format).deleteCharAt(charPos).toString();
		    
		    Bukkit.broadcastMessage(format);
		} 
		// Envoie le message uniquement à ceux de la meme équipe
		else if (message.startsWith(teamMessagePrefix)) {
			e.setCancelled(true);
						
			String format = e.getFormat();
			
			int charPos = format.indexOf(teamMessagePrefix);
		    if (charPos > 0)
		    	format = new StringBuilder(format).deleteCharAt(charPos).toString();
			
			for (String playerName : t.getEntries()) {
				Player teamMate = Bukkit.getServer().getPlayer(playerName);
				teamMate.sendMessage(format);
			}
		} 
		// Annule le message si rien n'est indiquer
		else
			e.setCancelled(true);
		
		return e;
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
	
	/**
	 * Attribut les scoreboards a un joueur
	 * 
	 * @param player Joueur a setup
	 */
	private void setupPlayerInfos(Player player) {
		player.setGameMode(GameMode.ADVENTURE);
		
		if (player.isOp()) {
			this.admins.addEntry(player.getName());
			player.setGameMode(GameMode.CREATIVE);
		}

		player.setScoreboard(this.board);
	}
}