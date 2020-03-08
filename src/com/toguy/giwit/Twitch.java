package com.toguy.giwit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.gson.Gson;

public class Twitch {
	
	// Constantes
	private final String CLIENT_ID = "lmblf1legdusdnjju7cx7t5qv5e7m5";
	private final String STREAM_INFOS_URL = "https://api.twitch.tv/helix/streams";
	private final Integer REQUEST_TIMEOUT = 1000;
	
	// Champs
	private Map<String, String> _playerTwitchName;
	
	/**
	 * Constructeur vide
	 */
	public Twitch() {
		this._playerTwitchName = new HashMap<>();
	}
	
	/**
	 * Récupère le pseudo twitch avec le nom du joueur
	 * 
	 * @param player_name nom du joueur
	 * @return
	 */
	public String getPlayerInPlayerTwichName(String player_name) {
		return this._playerTwitchName.get(player_name);
	}
	
	/**
	 * Ajout un nouveau joueur dans la liste des joueurs -> nom twitch
	 * 
	 * @param player_name Nom du joueur
	 * @param twich_name Nom twitch
	 */
	public void addPlayerInPlayerTwichName(String player_name, String twich_name) {
		this._playerTwitchName.put(player_name, twich_name);
	}
	
	/**
	 * Supprime le joueur de la liste
	 * 
	 * @param payer_name Nom du joueur
	 */
	public void removePlayerInPlayerTwitchname(String player_name) {
		this._playerTwitchName.remove(player_name);
	}
	
	/**
	 * Récupère les informations d'un stream par le nom du streamer
	 * 
	 * @param user_login Pseudo du streamer
	 * @return La réponse de la requete
	 * @throws Exception
	 */
	public @Nullable Stream getStreamInfosByUserLogin(String user_login) throws Exception {
		try {
			URL url = new URL(STREAM_INFOS_URL + "?user_login=" + user_login);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			
			con.setRequestMethod("GET");
			con.setRequestProperty("Client-ID", CLIENT_ID);
			
			con.setReadTimeout(REQUEST_TIMEOUT);
			con.setDoOutput(true);
			
			con.connect();
			
			Integer status = con.getResponseCode();
			
			switch (status) {
				case 200:
					BufferedReader in = new BufferedReader(
						new InputStreamReader(con.getInputStream())
					);
					
					String inputLine;
					StringBuffer response = new StringBuffer();
					
					while ((inputLine = in.readLine()) != null)
						response.append(inputLine);
					
					in.close();
					
					con.disconnect();
					
					JSONParser parser = new JSONParser();
					JSONObject json = (JSONObject)parser.parse(response.toString());
					
					Bukkit.getServer().getLogger().info(response.toString());
					
					JSONArray data = (JSONArray)json.get("data");

					if (!data.isEmpty()) {
						Gson g = new Gson();
						Stream stream = g.fromJson(data.get(0).toString(), Stream.class);
						
						return stream;
					} else
						return null;
				default:
					throw new Exception("Erreur lors de la requete. Code: " + status);
			}
			
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * Classe pour l'encapsulation des streams
	 * 
	 * @author Tanguy Cavagna
	 * @version 1.0
	 */
	public static class Stream {
		private Integer id;
		private Integer user_id;
		private String user_name;
		private String type;
		private String title;
		private Integer viewer_count;
		
		public Integer getId() { return this.id; }
		public void setId(Integer pId) { this.id = pId; }
		
		public Integer getUserId() { return this.user_id; }
		public void setUserId(Integer pUserId) { this.user_id = pUserId; }
		
		public String getUserName() { return this.user_name; }
		public void setUserName(String pUserName) { this.user_name = pUserName; }
		
		public String getType() { return this.type; }
		public void setType(String pType) { this.type = pType; }
		
		public String getTitle() { return this.title; }
		public void setTitle(String pTitle) { this.title = pTitle; }
		
		public Integer getViewerCount() { return this.viewer_count; }
		public void setViewerCount(Integer pViewerCount) { this.viewer_count = pViewerCount; }
		
		public Boolean isLive() { return this.type.equalsIgnoreCase("live"); }
		
		@Override
		public String toString() {
			return this.title;
		}
	}
	
}
