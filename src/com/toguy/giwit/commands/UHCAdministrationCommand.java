package com.toguy.giwit.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.toguy.giwit.Episode;
import com.toguy.giwit.GiWit;
import com.toguy.giwit.scoreboards.uhc.TeamScoreboards;
import com.toguy.giwit.scoreboards.uhc.UHCTeam;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class UHCAdministrationCommand implements CommandExecutor, Listener {
	
	// Champs
	private JavaPlugin plugin = GiWit.getPlugin(GiWit.class);
	
	private Scoreboard board;
	private Random random;
	
	private Boolean moving;
	private int startSize;
	private int endSize;
	private int timeToShrink;
	private int timeBeforeShrink;
	private int timeToStartWhenReady;
	
	private Episode episode;
	private int episodeTimeUpdater = 0;
	
	private int timeBeforePvp;
	private Boolean isPvpEnable = false;
	private Boolean startPvpTimer = false;
	private int resistanceTime = 10;
	
	private Boolean isNaturalRegenerationEnable;
	private int covidDamageAmount;
	
	private Boolean uhcStarted = false;
	
	private World world;
	
	// Variables
	public WorldBorder wb;
	
	/**
	 * Constructeur uniquement pour récupérer le scoreboard
	 */
	public UHCAdministrationCommand() {
		this.board = TeamScoreboards.getInstance().getScoreboard();
		this.random = new Random();
	}
	
	// Public
	//=============================
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		this.getConfig();
		
		if (sender instanceof Player) {
			Player player = (Player)sender;
			
			if (args.length <= 0) {
				player.sendMessage("");
				player.sendMessage(this.alternateColorForString(ChatColor.GRAY, ChatColor.WHITE, "☰☰☰☰☰☰☰☰") + ChatColor.AQUA + " /uhc" + ChatColor.RED + " [OP] " + this.alternateColorForString(ChatColor.GRAY, ChatColor.WHITE, "☰☰☰☰☰☰☰☰"));
				player.sendMessage(ChatColor.DARK_GRAY + "> " + ChatColor.AQUA + "scenarios: " + ChatColor.WHITE + "Affiche la liste des scenarios activés et désactivés");
				player.sendMessage(ChatColor.DARK_GRAY + "> " + ChatColor.AQUA + "remake: " + ChatColor.WHITE + "Relance un uhc sur la même map");
				player.sendMessage(ChatColor.DARK_GRAY + "> " + ChatColor.AQUA + "remake world: " + ChatColor.WHITE + "Relance un uhc sur un nouveau monde");
				player.sendMessage(ChatColor.DARK_GRAY + "> " + ChatColor.AQUA + "start: " + ChatColor.WHITE + "Commence l'uhc");
				player.sendMessage(ChatColor.DARK_GRAY + "> " + ChatColor.AQUA + "shrink: " + ChatColor.WHITE + "Commence le retrecissement des bordures");
				
				return true;
			}
			
			// Affiche tout les scenarios actifs
			if (args[0].equalsIgnoreCase("scenarios")) {
				if (sender instanceof Player) {
					ArrayList<String> activated = new ArrayList<String>();
					ArrayList<String> disabled = new ArrayList<String>();
					
					if (this.plugin.getConfig().getBoolean("scenarios.cut-clean"))
						activated.add("CutClean");
					else
						disabled.add("CutClean");
					
					player.sendMessage(ChatColor.GOLD + "------------- " + ChatColor.DARK_AQUA + "Scenarios " + ChatColor.GOLD + "-------------");
					player.sendMessage(ChatColor.DARK_AQUA + "Activés : " + (activated.size() > 0 ? ChatColor.DARK_GREEN +  String.join(", ", activated) : ChatColor.WHITE + "Aucun"));
					player.sendMessage(ChatColor.DARK_AQUA + "Désactivés : " + (disabled.size() > 0 ? ChatColor.RED +  String.join(", ", disabled) : ChatColor.WHITE + "Aucun"));
					player.sendMessage(ChatColor.GOLD + "-----------------------------------");
				}
			}
			
			// Re créer un monde
			if (args[0].equalsIgnoreCase("remake")) {
				if (sender instanceof Player) {
					if (((Player)sender).isOp()) {
						this.uhcStarted = false;
						Bukkit.getScheduler().cancelTask(episodeTimeUpdater);
						
						Object lock = new Object();
						
						synchronized (lock) {
							if (args.length > 1 && !args[1].isEmpty())
								this.createWorld(args[1]);
							else {
								// Si le monde n'a pas été chargé, va le chercher dans les mondes.
								// Si il n'existe pas, demande au joueur de le créer
								if (world == null) {
									world = Bukkit.getServer().createWorld(new WorldCreator("UHC"));
									
									if (world == null) {
										this.sendClickableCommandToPlayer("Aucun monde UHC existant. Veuillez faire ", "/uhc remake world", " pour générer le monde !", player);
										return true;
									}
								}
								
								this.createWorld("");
							}
						}
						
						for (Player p : Bukkit.getOnlinePlayers()) {
							p.teleport(world.getSpawnLocation());
							p.setScoreboard(this.board);
							p.getInventory().clear();
							p.getInventory().setItem(0, new ItemStack(Material.COMPASS));
							
							if (!player.isOp())
								player.setGameMode(GameMode.ADVENTURE);
							else
								player.setGameMode(GameMode.CREATIVE);
						}
					}
				}
			}
			
			// Fait rapetisser le monde
			if (args[0].equalsIgnoreCase("shrink")) {
				if (this.moving) {
					if (wb != null)
						wb.setSize(this.endSize * 2, this.timeToShrink);
					else
						this.sendClickableCommandToPlayer("Tu dois en premier lieu commencer la partie avec la commande : ", "/uhc start", "", player);
				}
			}
			
			// Commence la partie
			if (args[0].equalsIgnoreCase("start")) {

				this.isPvpEnable = false;
				
				if (this.board.getObjective(DisplaySlot.PLAYER_LIST) != null)
					this.board.getObjective(DisplaySlot.PLAYER_LIST).unregister();
				
				Objective health = this.board.registerNewObjective("Health", "health", "Vie");
				health.setRenderType(RenderType.HEARTS);
				health.setDisplaySlot(DisplaySlot.PLAYER_LIST);

				for (Player p : Bukkit.getOnlinePlayers()) {
					p.setScoreboard(this.board);
				}
				
				// Compte a rebour avant début de partie
				int startCountdown = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, new Runnable() {

					private int countdown = timeToStartWhenReady;
					
					@Override
					public void run() {
						countdown--;
						
						Bukkit.broadcastMessage(getServerMessagePrefix() + ChatColor.DARK_GREEN + "Début de la partie dans " + ChatColor.GREEN + countdown + ChatColor.DARK_GREEN + " secondes");
					}
					
				}, 0L, 20L);
				
				// S'éxécute après le compte a rebour de début de partie
				Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
					@Override
					public void run() {
						Bukkit.getScheduler().cancelTask(startCountdown);
						
						if (moving) {
							if (world != null) {
								uhcStarted = true;
								
								// Fait bouger la border après un durée donnée et pendant une durée donnée
								Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
									@Override
									public void run() {
										wb.setSize(endSize * 2, timeToShrink);
									}
								}, timeBeforeShrink * 20);
							}
							else
								sendClickableCommandToPlayer("Tu dois en premier lieu re générer le monde avec la commande : ", "/uhc remake", "", player);
						}
						
						teleportAllPlayers();
						
						// Active le timer du pvp après la resistance au dégats
						Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
							
							@Override
							public void run() {
								startPvpTimer = true;
								
								Bukkit.broadcastMessage(getServerMessagePrefix() + ChatColor.DARK_AQUA + "Les dégats (sauf PVP) sont désormais activés !");
								Bukkit.broadcastMessage(getServerMessagePrefix() + ChatColor.DARK_AQUA + "PVP activé dans " + (timeBeforePvp / 60) + " minutes !");
							}
							
						}, resistanceTime * 20);
						
						if (!isNaturalRegenerationEnable)
							player.performCommand("gamerule naturalRegeneration false");
						else
							player.performCommand("gamerule naturalRegeneration true");
					}
				}, this.timeToStartWhenReady * 20);
			}
		}
		
		return true;
	}

	/**
	 * Gentleman rule
	 * 
	 * @param e
	 */
	@EventHandler
	public void onAttack(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			if (this.isPvpEnable) {
				// Doc nothing
			} else {
				e.setCancelled(true);
			}
		}
		
		// Si le joueur se fait toucher par une fleche, joue le son "ding" pour le tireur
		if (e.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE) ) {
			Arrow a = (Arrow)e.getDamager();
			
			if (a.getShooter() instanceof Player && e.getEntity() instanceof Player) {
				Player player = (Player)a.getShooter();
				
				player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 1);
			}
		}
	}
	
	/**
	 * Désactive les drops d'items si la game a pas commencer
	 * 
	 * @param e
	 */
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent e) {
		if (!this.uhcStarted) {
			e.setCancelled(true);
		}
	}
	
	/**
	 * Téléporte les joueurs lors de leur join
	 * 
	 * @param e
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = (Player)e.getPlayer();
		
		if (!player.isOp())
			player.setGameMode(GameMode.ADVENTURE);
		
		player.teleport(this.world.getSpawnLocation());
	}
	
	/**
	 * Met en spec chaque joueurs mort
	 * 
	 * @param e
	 */
	@EventHandler
	public void onPlayerDeath(PlayerRespawnEvent e) {
		Player player = (Player)e.getPlayer();

		e.setRespawnLocation(player.getWorld().getSpawnLocation());
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				player.setGameMode(GameMode.SPECTATOR);
			}
			
		}, 2);
		
		this.doesATeamHasWin();
	}

	// Private
	//=============================
	/**
	 * Créer un nouveau monde à chaque lancement de serveur
	 */
	private void createWorld(String remakeWorld) {	
		if (remakeWorld.equalsIgnoreCase("world")) {
			Bukkit.broadcastMessage(this.getServerMessagePrefix() + ChatColor.DARK_AQUA + "Le serveur recréer un monde. Veuillez ne plus lancer de commandes jusqu'à ce que le monde soit terminé !");
			
			World delete = Bukkit.getWorld("UHC");
			
			for (Player player : Bukkit.getOnlinePlayers())
				player.teleport(Bukkit.getWorld("world").getSpawnLocation());
			
			if (delete != null) {
				
				File deleteFolder = delete.getWorldFolder();
				
				Bukkit.unloadWorld(delete, false);
				this.deleteWorld(deleteFolder);
			}

			WorldCreator creator = new WorldCreator("UHC");
			creator.generateStructures(true);
			world = creator.createWorld();
			
			this.generateSpawnPlatform();
			this.createWorldBorder(0, 0, this.startSize);
			
			for (Player player : Bukkit.getOnlinePlayers())
				player.teleport(world.getSpawnLocation());
			
			this.createScoreboard();
			
			Bukkit.broadcastMessage(this.getServerMessagePrefix() + ChatColor.DARK_AQUA + "Monde correctement créer !");
		} else {
			world = Bukkit.getWorld("UHC");
			
			this.generateSpawnPlatform();
			this.createWorldBorder(0, 0, this.startSize);
			
			this.createScoreboard();
			
			Bukkit.broadcastMessage(this.getServerMessagePrefix() + ChatColor.DARK_AQUA + "Partie correctement recréer !");
		}
	}

	/**
	 * Ecrit un message si une équipe a gagner
	 * 
	 * @return
	 */
	private Boolean doesATeamHasWin() {
		List<Team> teamsAlive = new ArrayList<Team>();
		for (UHCTeam uhcTeam : TeamScoreboards.getInstance().getTeams().values()) {
			Team team = uhcTeam.getTeam();
			
			for (String playerName : team.getEntries()) {
				Player p = Bukkit.getPlayer(playerName);
				
				if (p != null) {
					if (p.getHealth() > 0) {
						teamsAlive.add(team);
						break;
					}
				}
			}
		}
		
		if (this.uhcStarted == true && teamsAlive.size() == 1) {
			Bukkit.broadcastMessage(this.getServerMessagePrefix() + ChatColor.GOLD + " GG à la team " + teamsAlive.get(0).getColor() + teamsAlive.get(0).getName() + ChatColor.GOLD + " pour avoir gagné la partie !");
			this.uhcStarted = false;
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Supprime un monde
	 * 
	 * @param path
	 * @return
	 */
 	public synchronized boolean deleteWorld(File path) {
		if(path.exists()) {
		    File files[] = path.listFiles();
		    for(int i=0; i<files.length; i++) {
		        if(files[i].isDirectory()) {
		            deleteWorld(files[i]);
		        } else {
		            files[i].delete();
		        }
		    }
		}
		
		return(path.delete());
	}

	/**
	 * Pré génère les chunks
	 */
	@Deprecated
	private void pregenerateChunks() {
		for (Player player : Bukkit.getOnlinePlayers())
			player.kickPlayer("Monde en prcessus de génération.");
		
		for (int i = 0; i < (int)(this.wb.getSize() / 16); i++) {
			for (int j = 0; j < (int)(this.wb.getSize() / 16); j++) {
				this.world.getChunkAt(i, j);
			}
		}
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
		wb.setSize(distanceFromCenter * 2);
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
		this.timeBeforePvp = this.plugin.getConfig().getInt("gentlemen-rule");
		this.isNaturalRegenerationEnable = this.plugin.getConfig().getBoolean("enable-health-regen");
		this.covidDamageAmount = this.plugin.getConfig().getInt("covid-damage-amount");
	}
	
	/**
	 * Créer le scoreboard de la sidebar
	 */
	private void createScoreboard() {
		// Recréer l'instance de l'episode pour le remttre a zero
		this.episode = new Episode();
		
		// Supprimer les objectifs et teams présendentes
		if (this.board.getObjective(DisplaySlot.SIDEBAR) != null)
			this.board.getObjective(DisplaySlot.SIDEBAR).unregister();
		
		if (this.board.getTeam("episode") != null)
			this.board.getTeam("episode").unregister();
		
		if (this.board.getTeam("timeLeft") != null)
			this.board.getTeam("timeLeft").unregister();
		
		if (this.board.getTeam("worldBorder") != null)
			this.board.getTeam("worldBorder").unregister();
		
		if (this.board.getTeam("pvp") != null)
			this.board.getTeam("pvp").unregister();
		
		// Créer notre scoreboard de sidebar
		Objective objective = this.board.registerNewObjective("GiWit", "dummy", ChatColor.WHITE + "" + ChatColor.BOLD + "GiWit");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		Team episodeNumber = this.board.registerNewTeam("episode");
		episodeNumber.addEntry(ChatColor.AQUA + "Episode ");
		episodeNumber.setPrefix("");
		episodeNumber.setSuffix(episode.getEpisodeNbr() + "");
		
		Team episodeTimeLeft = this.board.registerNewTeam("timeLeft");
		episodeTimeLeft.addEntry(ChatColor.GOLD + "Temps restant: " + ChatColor.YELLOW);
		episodeTimeLeft.setPrefix("");
		episodeTimeLeft.setSuffix(episode.getTimeLeftHasString() + "");
		
		Team pvp = this.board.registerNewTeam("pvp");
		pvp.addEntry(ChatColor.RED + "PVP: ");
		pvp.setPrefix("");
		if (timeBeforePvp == -1)
			pvp.setSuffix(ChatColor.GREEN + "✔");
		else
			pvp.setSuffix(ChatColor.DARK_RED + "✖");
		
		Team worldBorderInfo = this.board.registerNewTeam("worldBorder");
		worldBorderInfo.addEntry(ChatColor.DARK_GREEN + "Bordures: " + ChatColor.GREEN);
		worldBorderInfo.setPrefix("");
		worldBorderInfo.setSuffix("+" + (int)((int)wb.getSize() / 2) + "/-" + (int)((int)wb.getSize() / 2));
		
		objective.getScore("").setScore(7);
		objective.getScore(ChatColor.AQUA + "Episode ").setScore(6);
		objective.getScore(ChatColor.GOLD + "Temps restant: " + ChatColor.YELLOW).setScore(5);
		objective.getScore(" ").setScore(4);
		objective.getScore(ChatColor.DARK_GREEN + "Bordures: " + ChatColor.GREEN).setScore(3);
		objective.getScore("  ").setScore(2);
		objective.getScore(ChatColor.RED + "PVP: ").setScore(1);
		objective.getScore("   ").setScore(0);

		// Met a jour les informations de l'espisode
		episodeTimeUpdater = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, new Runnable() {
			@Override
			public void run() {
				if (uhcStarted) {
					episode.updateTimeLeft();
					
					episodeTimeLeft.setSuffix(episode.getTimeLeftHasString() + "");
	
					if (episode.getTimeLeft() <= 0) {
						Bukkit.broadcastMessage(getServerMessagePrefix() + ChatColor.DARK_AQUA + "------------- Fin de l'épisode " + episode.getEpisodeNbr() + " -------------");
						episode.nextEpisode();
						episodeNumber.setSuffix(episode.getEpisodeNbr() + "");
						
						UHCTeam uhcTeam = (UHCTeam)TeamScoreboards.getInstance().getTeams().values().toArray()[random.nextInt(TeamScoreboards.getInstance().getTeams().size())];
						Team t = uhcTeam.getTeam();
						
						while (t.getEntries().size() <= 0) {
							uhcTeam = (UHCTeam)TeamScoreboards.getInstance().getTeams().values().toArray()[random.nextInt(TeamScoreboards.getInstance().getTeams().size())];
							t = uhcTeam.getTeam();
						}
						
						Bukkit.broadcastMessage(getServerMessagePrefix() + ChatColor.DARK_AQUA + "Les joueurs de l'équipe " + t.getColor() + t.getDisplayName() + ChatColor.DARK_AQUA + " ont été testé positif au covid-19 et ont perdu " + ((int)(covidDamageAmount / 2)) + " coeurs !");
						
						for (String playerName : t.getEntries()) {
							Player p = Bukkit.getPlayer(playerName);

							if (p != null)
								p.setHealth(p.getHealth() - covidDamageAmount);
						}
					}
					
					worldBorderInfo.setSuffix("+" + (int)((int)wb.getSize() / 2) + "/-" + (int)((int)wb.getSize() / 2));
					
					if (startPvpTimer) {
						if (timeBeforePvp != -1) {
							if (timeBeforePvp <= 0) {
								if (!isPvpEnable)
									Bukkit.broadcastMessage(getServerMessagePrefix() + ChatColor.DARK_AQUA + "Le pvp est maintenant " + ChatColor.BOLD + "" + ChatColor.DARK_GREEN + " ACTIF " + ChatColor.RESET + "" + ChatColor.DARK_AQUA + "!");
								
								pvp.setSuffix(ChatColor.GREEN + "✔");
								isPvpEnable = true;
							} else {
								pvp.setSuffix(ChatColor.DARK_RED + "✖");
								timeBeforePvp--;
							}
						}
					}
				}
			}
		}, 20, 20);
	}
	
	/**
	 * Téléporte tout les joueurs dans la map
	 */
	private void teleportAllPlayers() {
		Random r = new Random();
		
		for (UHCTeam uhcTeam : TeamScoreboards.getInstance().getTeams().values()) {
			Team team = uhcTeam.getTeam();
			
			int x = r.nextInt((int)this.wb.getSize()) - (int)(this.wb.getSize() / 2);
			int z = r.nextInt((int)this.wb.getSize()) - (int)(this.wb.getSize() / 2);
			Location randomSpawn = new Location(this.world, (double)(x), 150.0, (double)(z));
			
			for (String playerName : team.getEntries()) {
				Player p = Bukkit.getPlayer(playerName);
				p.getInventory().setItem(0, null);
				p.teleport(randomSpawn);
				p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, this.resistanceTime * 20, 100));
			}
		}
	}

	/**
	 * Retourne une chaine de caractère avec des couleurs en altérnance
	 * @return
	 */
	private String alternateColorForString(ChatColor color1, ChatColor color2, String foo) {
		String result = "";
		
		for (int i = 0; i < foo.length(); i++) {
			if (i % 2 == 0) {
				result += color1 + String.valueOf(foo.charAt(i));
			} else {
				result += color2 + String.valueOf(foo.charAt(i));
			}
		}
		
		return result;
	}

	/**
	 * Retourne le prefix pour les message venant du plugin
	 * @return
	 */
	private String getServerMessagePrefix() {
		return ChatColor.AQUA + "[" + ChatColor.GOLD + "" + ChatColor.BOLD + "UHC" + ChatColor.RESET + "" + ChatColor.AQUA + "] ";
	}
}