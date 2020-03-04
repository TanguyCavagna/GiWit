package com.toguy.giwit.scoreboards.uhc;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import com.toguy.giwit.GiWit;

/**
 * 
 * Gère toutes les équipes de l'UHC
 * 
 * @author TanguyCavagna
 *
 */
public class TeamScoreboards {

	//===================== Start SingleTone =====================
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

	//===================== Start Champs =====================
	private Scoreboard teamScoreboard;
	private HashMap<String, UHCTeam> teams;
	private JavaPlugin plugin; 
	
	//===================== Start Propiétés =====================
	public HashMap<String, UHCTeam> getTeams() {
		return this.teams;
	}
	
	//===================== Start Fonctions =====================
	/**
	 * Constructeur
	 */
	public TeamScoreboards() {
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		
		this.teamScoreboard = manager.getNewScoreboard();
		this.teams = new HashMap<String, UHCTeam>();
		this.plugin = GiWit.getPlugin(GiWit.class);
		
		// Créer les team depuis le fichier de config
		for (int i = 0; i < this.plugin.getConfig().getInt("team-count"); i++) {
			String name = this.plugin.getConfig().getString("teams.team-" + (i + 1) + ".name");
			String color = this.plugin.getConfig().getString("teams.team-" + (i + 1) + ".color");
			String material = this.plugin.getConfig().getString("teams.team-" + (i + 1) + ".material");
			int maximumMembers = this.plugin.getConfig().getInt("teams.team-" + (i + 1) + ".maximum-members");
						
			this.teams.put(name, new UHCTeam(name, ChatColor.valueOf(color), Material.valueOf(material), maximumMembers, this.teamScoreboard));
		}
		
		//this.teams.put("Rouge", new UHCTeam("Rouge", ChatColor.RED, Material.RED_WOOL, 1, this.teamScoreboard));
		//this.teams.put("Bleu", new UHCTeam("Bleu", ChatColor.BLUE, Material.BLUE_WOOL, 2, this.teamScoreboard));
		//this.teams.put("Vert", new UHCTeam("Vert", ChatColor.GREEN, Material.LIME_WOOL, 2, this.teamScoreboard));
		//this.teams.put("Violet", new UHCTeam("Violet", ChatColor.DARK_PURPLE, Material.PURPLE_WOOL, 2, this.teamScoreboard));
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
				
		this.teams.get(ChatColor.stripColor(teamToJoin)).getTeam().addEntry(player.getName());
		
		return this.teamScoreboard;
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
