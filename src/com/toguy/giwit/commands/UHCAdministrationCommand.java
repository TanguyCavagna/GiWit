package com.toguy.giwit.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.toguy.giwit.GiWit;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class UHCAdministrationCommand implements CommandExecutor {
	
	private JavaPlugin plugin = GiWit.getPlugin(GiWit.class);
	
	// Variable de la configuration
	private Boolean moving;
	private int startSize;
	private int endSize;
	private int timeToShrink;
	private int timeBeforeShrink;
	private int timeToStartWhenReady;
	
	private World world;
	public WorldBorder wb;
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		this.getConfig();
		
		if (sender instanceof Player) {
			Player player = (Player)sender;
			
			// Re créer un monde
			if (args[0].equalsIgnoreCase("remake")) {
				if (sender instanceof Player) {
					if (((Player)sender).isOp()) {
						createWorld();
						
						for (Player p : Bukkit.getOnlinePlayers())
							p.teleport(world.getSpawnLocation());
					}
				}
			}
			
			// Fait rapetisser le monde
			if (args[0].equalsIgnoreCase("shrink")) {
				if (this.moving) {
					if (wb != null)
						wb.setSize(this.endSize, this.timeToShrink);
					else
						this.sendClickableCommandToPlayer("Tu dois en premier lieu commencer la partie avec la commande : ", "/uhc start", "", player);
				}
			}
			
			// Commence la partie
			if (args[0].equalsIgnoreCase("start")) {

				// Compte a rebour avant début de partie
				int startCountdown = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, new Runnable() {

					private int countdown = timeToStartWhenReady;
					
					@Override
					public void run() {
						countdown--;
						
						Bukkit.broadcastMessage("Start in " + countdown + "s");
					}
					
				}, 0L, 20L);
				
				// S'éxécute après le compte a rebour de début de partie
				Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {

					@Override
					public void run() {
						Bukkit.getScheduler().cancelTask(startCountdown);
						
						if (moving) {
							if (world != null) {
								// Fait bouger la border après un durée donnée et pendant une durée donnée
								Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
									@Override
									public void run() {
										wb.setSize(endSize, timeToShrink);
									}
								}, timeBeforeShrink);
							}
							else
								sendClickableCommandToPlayer("Tu dois en premier lieu re générer le monde avec la commande : ", "/uhc remake", "", player);
						}
					}
					
				}, this.timeToStartWhenReady * 20);
			}
		}
		
		return true;
	}
	
	/**
	 * Créer un nouveau monde à chaque lancement de serveur
	 */
	private void createWorld() {
		//WorldCreator creator = new WorldCreator("UHC-" + UUID.randomUUID().toString().split("-")[0]);
		//creator.generateStructures(true);
		//world = creator.createWorld();
		
		world = Bukkit.getWorld("world");
		
		this.generateSpawnPlatform();
		this.createWorldBorder(0, 0, this.startSize);
	}

	/**
	 * Génère la plateforme de spawn
	 */
	private void generateSpawnPlatform() {
		world.setSpawnLocation(new Location(world, 0, 121, 0));
		
		this.generateGrid(
			Material.BLACK_STAINED_GLASS,
			Material.WHITE_STAINED_GLASS,
			new int[] {-10, 10},
			120,
			new int[] {-10, 10}
		);
		
		this.generateWalls(
			Material.GLASS_PANE,
			new int[] {-10, 10},
			121,
			new int[] {-10, 10},
			3
		);
	}
	
	/**
	 * Génère une grille de bloque en alternance en centre {0, 0}
	 * 
	 * @param material1 Materiel 1
	 * @param material2 Materiel 2
	 * @param xPoses Tableau des positions min et max pour la coordonée x
	 * @param zPoses Tableau des positions min et max pour la coordonée z
	 */
	private void generateGrid(Material material1, Material material2, int[] xPoses, int y, int[] zPoses) {
		for (int x = xPoses[0]; x <= xPoses[1]; x++) {
			for (int z = zPoses[0]; z <= zPoses[1]; z++) {
				if ((z % 2 == 0 && x % 2 == 0) || (z % 2 != 0 && x % 2 != 0))
					world.getBlockAt(new Location(world, x, y, z)).setType(material1);
	            else if ((z % 2 == 0 && x % 2 != 0) || (z % 2 != 0 && x % 2 == 0))
	            	world.getBlockAt(new Location(world, x, y, z)).setType(material2);
			}
		}
	}
	
	/**
	 * Génère des murs
	 * 
	 * @param material Materiel du mur
	 * @param xPoses Tableau des positions min et max pour la coordonée x
	 * @param zPoses Tableau des positions min et max pour la coordonée z
	 * @param height Hauteur du mur
	 */
	private void generateWalls(Material material, int[] xPoses, int y, int[] zPoses, int height) {
		for (int h = 0; h < height; h++) {
			for (int x = xPoses[0]; x <= xPoses[1]; x++) {
				for (int z = zPoses[0]; z <= zPoses[1]; z++) {
					if (x == xPoses[0] || z == zPoses[0] || x == xPoses[1] || z == zPoses[1])
						world.getBlockAt(new Location(world, x, y + h, z)).setType(material);
				}
			}
		}
	}

	/**
	 * Créer la bordure du monde
	 * 
	 * @param distanceFromCenter Distance du centre
	 */
	private void createWorldBorder(int x, int z, int distanceFromCenter) {
		wb = world.getWorldBorder();
		wb.setCenter(x, z);
		wb.setSize(distanceFromCenter);
	}

	/**
	 * Envoie un message avec une partie cliquable a un joueur
	 * 
	 * @param prefix Texte avant la partie cliquable
	 * @param command Commande a executer lors du clique
	 * @param sufix Texte après la partie cliquable
	 * @param player Joueur a qui envoyer
	 */
	private void sendClickableCommandToPlayer(String prefix, String command, String sufix, Player player) {
		TextComponent mainMsg = new TextComponent(prefix);
		mainMsg.setColor(ChatColor.WHITE);
		TextComponent link = new TextComponent(command);
		link.setUnderlined(true);
		link.setColor(ChatColor.GOLD);
		link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click me !").create()));
		link.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
		mainMsg.addExtra(link);
		mainMsg.addExtra(sufix);
		
		player.spigot().sendMessage(mainMsg);
	}

	/**
	 * Récupère la config du plugin
	 */
	private void getConfig() {
		this.moving = this.plugin.getConfig().getBoolean("border.moving");
		this.startSize = this.plugin.getConfig().getInt("border.start-size");
		this.endSize = this.plugin.getConfig().getInt("border.end-size");
		this.timeToShrink = this.plugin.getConfig().getInt("border.time-to-shrink");
		this.timeBeforeShrink = this.plugin.getConfig().getInt("border.time-before-shrink");
		this.timeToStartWhenReady = this.plugin.getConfig().getInt("time-to-start-when-ready");
	}
}