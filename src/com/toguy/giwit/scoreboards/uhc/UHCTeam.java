package com.toguy.giwit.scoreboards.uhc;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class UHCTeam {

	// Constantes
	private final String PREFIX = "⏺";
	
	//Champs
	private String _name;
	private ChatColor _color;
	private Material _item;
	private int _maxPlayers;
	private Team _team;
	
	//Proptiétés
	//........... Getter ...........
	public String getName() { return this._name; }
	
	public ChatColor getColor() { return this._color; }
	
	public Material getItem() { return _item; }
	
	public int getMaxPlayers() { return _maxPlayers; }
	
	public Team getTeam() { return _team; }

	//........... Setter ...........
	private void setName(String name) { this._name = name; }

	private void setColor(ChatColor color) { this._color = color; }

	private void setItem(Material _item) { this._item = _item; }

	private void setMaxPlayers(int _maxPlayers) { this._maxPlayers = _maxPlayers; }

	/**
	 * Constructeur
	 * 
	 * @param name Nom de l'équipe
	 * @param color Couleur de l'équipe
	 */
	public UHCTeam(String name, ChatColor color, Material item, int maxPlayers, Scoreboard mainScoreboard) {
		this.setName(name);
		this.setColor(color);
		this.setItem(item);
		this.setMaxPlayers(maxPlayers);
		
		this._team = mainScoreboard.registerNewTeam(this.getName());
		this._team.setColor(color);
		this._team.setPrefix(color + PREFIX);
	}
	
}
