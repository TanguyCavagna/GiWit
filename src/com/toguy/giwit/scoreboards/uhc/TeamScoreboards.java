package com.toguy.giwit.scoreboards.uhc;

import java.util.HashMap;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import com.toguy.giwit.GiWit;

/**
 * 
 * Gère toutes les équipes de l'UHC
 * 
 * @author TanguyCavagna
 *
 */
public class TeamScoreboards {
	
	// Champs
	private Scoreboard teamScoreboard;
	private HashMap<String, UHCTeam> teams;
	private Team streamers;
	private Team admins;
	private JavaPlugin plugin; 
	
	private static TeamScoreboards teamScoreboardInstance;
	
	/**
	 * Singleton pour le teams
	 * 
	 * @return
	 */
	public static TeamScoreboards getInstance() {
		if (teamScoreboardInstance == null)
			teamScoreboardInstance = new TeamScoreboards();
		
		return teamScoreboardInstance;
	}
	
	/**
	 * Supprime l'instance
	 */
	public static void purgeScoreboards() {
		teamScoreboardInstance = null;
	}
	
	// Propiétées
	public HashMap<String, UHCTeam> getTeams() {
		return this.teams;
	}
	
	/**
	 * Constructeur
	 */
	public TeamScoreboards() {
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		
		this.teamScoreboard = manager.getNewScoreboard();
		this.teams = new HashMap<String, UHCTeam>();
		this.plugin = GiWit.getPlugin(GiWit.class);
		
		this.streamers = this.teamScoreboard.registerNewTeam("Streamer");
		this.streamers.setSuffix(ChatColor.RED + " ⏺ LIVE ");
		this.streamers.setColor(org.bukkit.ChatColor.WHITE);
		
		this.admins = this.teamScoreboard.registerNewTeam("Admin");
		this.admins.setSuffix(ChatColor.GOLD + " [ADMIN]");
		this.admins.setColor(org.bukkit.ChatColor.WHITE);
		
		// Créer les team depuis le fichier de config
		for (int i = 0; i < this.plugin.getConfig().getInt("team-count"); i++) {
			String name = this.plugin.getConfig().getString("teams.team-" + (i + 1) + ".name");
			String color = this.plugin.getConfig().getString("teams.team-" + (i + 1) + ".color");
			String material = this.plugin.getConfig().getString("teams.team-" + (i + 1) + ".material");
			int maximumMembers = this.plugin.getConfig().getInt("teams.team-" + (i + 1) + ".maximum-members");
						
			this.teams.put(name, new UHCTeam(name, ChatColor.valueOf(color), Material.valueOf(material), maximumMembers, this.teamScoreboard));
		}
	}
	
	// Public
	//=============================
	public Scoreboard getScoreboard() {
		return this.teamScoreboard;
	}
	
	/**
	 * Ajoute un joueur dans une équipe
	 * 
	 * @param player Joueur a mettre dans une equipe
	 * @param teamToJoin Equipe a rejoindre
	 * @return Scoreboard mis a jour
	 */
	public Scoreboard addPlayerInTeam(Player player, String teamToJoin) {
		this.removePlayerFromAllTeams(player);
		
		Team playerTeam = this.teams.get(ChatColor.stripColor(teamToJoin)).getTeam();
		
		String playerDisplayNameWithoutPrefix = player.getDisplayName().substring(player.getDisplayName().indexOf(player.getName()));
		
		String displayName = playerTeam.getColor() + playerTeam.getPrefix() + playerDisplayNameWithoutPrefix;
		player.setDisplayName(displayName);
		player.setPlayerListName(displayName);
		
		this.teams.get(ChatColor.stripColor(teamToJoin)).getTeam().addEntry(player.getName());
		
		return this.teamScoreboard;
	}
	
	/**
	 * Ajoute un joueur a l'équipe des admins
	 * 
	 * @param player
	 */
	public void addPlayerToAdminTeam(Player player) {
		this.admins.addEntry(player.getName());
	}
	
	/**
	 * Est ce qu'un joueur fait parti des admins
	 * 
	 * @param player
	 * @return
	 */
	public Boolean isPlayerInAdmins(Player player) {
		return this.admins.hasEntry(player.getName());
	}
	
	/**
	 * Supprime un joueur des admins 
	 * 
	 * @param player
	 */
	public void removePlayerFromAdmins(Player player) {
		this.admins.removeEntry(player.getName());
	}
	
	@Nullable
	public Team getPlayerTeam(Player player) {
		for (UHCTeam team : this.teams.values()) {
			if (team.getTeam().hasEntry(player.getName())) {
				return team.getTeam();
			}
		}
		
		return null;
	}
	
	/**
	 * Supprime un joueur de toutes les équipes présentes
	 * 
	 * @param player
	 */
	public void removePlayerFromAllTeams(Player player) {
		for (UHCTeam team : this.teams.values())
			team.getTeam().removeEntry(player.getName());
	}
	
	/**
	 * Récupère tous les joueurs d'une equipe
	 * 
	 * @return
	 */
	public Set<String> getPlayerInTeam(String teamToRetrieve) {
		return this.teams.get(ChatColor.stripColor(teamToRetrieve)).getTeam().getEntries();
	}

}
