package com.toguy.pastaplugin;

import org.bukkit.Bukkit;
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
import com.toguy.pastaplugin.commands.GUICommand;
import com.toguy.pastaplugin.commands.RegenerateWorldCommand;
import com.toguy.pastaplugin.commands.SwapCommand;
import com.toguy.pastaplugin.events.ClickGuiEvent;
import com.toguy.pastaplugin.events.UHCEvent;
import com.toguy.pastaplugin.scoreboards.uhc.TeamScoreboards;

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
		// Setup des commandes
		getCommand("regenerate").setExecutor(new RegenerateWorldCommand());
		getCommand("gui").setExecutor(new GUICommand());
		getCommand("swap").setExecutor(new SwapCommand());
		
		// Setup de events
		Bukkit.getServer().getPluginManager().registerEvents(new UHCEvent(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new ClickGuiEvent(), this);
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		
		// Debug message
		Bukkit.getServer().getLogger().info("Player-Player plugin is enable");
		
		// Setup des scoreboards
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		
		this.board = manager.getNewScoreboard();
		this.streamers = board.registerNewTeam("Streamer");
		this.streamers.setColor(org.bukkit.ChatColor.WHITE);
		
		this.admins = board.registerNewTeam("Admin");
		this.admins.setSuffix(ChatColor.GOLD + " [ADMIN]");
		this.admins.setColor(org.bukkit.ChatColor.WHITE);
		
		TeamScoreboards.purgeScoreboards();
		
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
			this.setupPlayerScorboard(player);
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
									this.streamers.setSuffix(ChatColor.RED + " ● LIVE " + ChatColor.LIGHT_PURPLE + "(" + stream.getViewerCount().toString() + ")");
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
				
		if (this.streamers.getEntries().contains(player.getName()))
			e.setFormat(ChatColor.WHITE + this.twitch.getPlayerInPlayerTwichName(player.getName()) + ChatColor.GRAY + " (" + player.getName() + ")" + ChatColor.RED + " [LIVE]" + ChatColor.WHITE + " : " + message);
		else
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
		this.setupPlayerScorboard(player);
	}
	
	/**
	 * Attribut les scoreboards a un joueur
	 * 
	 * @param player Joueur a setup
	 */
	private void setupPlayerScorboard(Player player) {
		if (player.isOp()) {
			this.admins.addEntry(player.getName());
			player.setScoreboard(this.board);
		}
	}
}