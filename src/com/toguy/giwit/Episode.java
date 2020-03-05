package com.toguy.giwit;

import org.bukkit.plugin.java.JavaPlugin;

public class Episode {

	// Champs
	private int episodeNbr;
	private int episodeDuration;
	private int timeLeft;
	private JavaPlugin plugin;
	
	/**
	 * Constructeur
	 * 
	 * @param pEpisodeNbr Numéro de l'épisode
	 * @param pTimeLeft Temps restant avant la fin de l'épisode en secondes
	 */
	public Episode() {
		this.plugin = GiWit.getPlugin(GiWit.class);
		this.episodeNbr = 1;
		this.episodeDuration = this.plugin.getConfig().getInt("episode-markers.delay");
		this.timeLeft = this.episodeDuration;
	}
	
	/**
	 * Met a jour le temps restant en retirant 1 seconde
	 */
	public void updateTimeLeft() {
		this.timeLeft--;
	}
	
	/**
	 * Récupère le tems restant en secondes
	 * 
	 * @return
	 */
	public int getTimeLeft() {
		return this.timeLeft;
	}
	
	/**
	 * Récupère le tems restant en minutes:secondes
	 * 
	 * @return mm:ss
	 */
	public String getTimeLeftHasString() {
		int minutes = (int)Math.floor(this.timeLeft / 60);
		int seconds = (this.timeLeft % 60);
		return (minutes > 0 ? this.intTo2DigitFormat(minutes) + "min " : "") + this.intTo2DigitFormat(seconds) + "s";
	}
	
	/**
	 * Récupère le numéro de l'épisode
	 * 
	 * @return
	 */
	public int getEpisodeNbr() {
		return this.episodeNbr;
	}
	
	/**
	 * Passe à l'épisode suivant
	 */
	public void nextEpisode() {
		this.episodeNbr++;
		this.timeLeft = this.episodeDuration;
	}
	
	/**
	 * Transforme un entier en chaine de caractère de deux de long
	 * 
	 * @param a Entier a convertir
	 * @return
	 */
	private String intTo2DigitFormat(int a) {
		return a < 10 ? "0" + a : "" + a;
	}
	
}
