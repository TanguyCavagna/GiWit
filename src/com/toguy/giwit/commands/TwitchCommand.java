package com.toguy.giwit.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.scoreboard.Team;

import com.toguy.giwit.Twitch;
import com.toguy.giwit.scoreboards.uhc.TeamScoreboards;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class TwitchCommand implements CommandExecutor {

	// Champs
	private Twitch twitch = new Twitch();
	
	// Public
	//=============================
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player)sender;
			
			if (args.length == 0) {
				p.sendMessage(ChatColor.BLUE + "Si tu veux te lier a une chaine, fait /twitch link <chaine>. Si tu veux dé lié, fait /twitch unlink");
				return true;
			}
			
			// Lie le joueur avec le stream
			if (args[0].equalsIgnoreCase("link")) {
				if (args.length > 1 && !args[1].isEmpty()) {
					// TODO : Décommenter les lignes ce dessous pour réactiver la permission
					//if (!p.isOp()) {
						try {
							Twitch.Stream stream = this.twitch.getStreamInfosByUserLogin(args[1]);

							if (stream != null && stream.isLive()) {
								String viewers = ChatColor.LIGHT_PURPLE + "(" + stream.getViewerCount().toString() + ")";
								String playerSuffix = p.getName() + ChatColor.GRAY + " (" + stream.getUserName() + ")" + ChatColor.RED + " [LIVE] " + ChatColor.WHITE;
								p.setDisplayName(playerSuffix);
								p.setPlayerListName(playerSuffix);
								
								Team playerTeam = TeamScoreboards.getInstance().getPlayerTeam(p);
								if (playerTeam != null) {
									String playerDisplayNameWithoutPrefix = p.getDisplayName().substring(p.getDisplayName().indexOf(p.getName()));
									String playerName = playerTeam.getColor() + playerTeam.getPrefix() + playerDisplayNameWithoutPrefix;
									p.setDisplayName(playerName);
									p.setPlayerListName(playerName);
								}
								
								p.setScoreboard(TeamScoreboards.getInstance().getScoreboard());
								
								this.twitch.addPlayerInPlayerTwichName(p.getName(), stream.getUserName());
								
								p.spigot().sendMessage(this.createTwitchLinkMessage(args[1]));
							} else {
								p.sendMessage(ChatColor.RED + "[Error]" + ChatColor.WHITE + "Le live de " + args[1] + " n'est pas on.");
							}
						} catch (Exception e) {
							p.sendMessage(ChatColor.DARK_RED + e.getMessage());
						}
					//} else {
					//	p.sendMessage("Tu es admin donc pas d'autre grade pour toi.");
					//}
				}
			}
			
			// Retire le lien entre le joueur et le stream
			if (args[0].equalsIgnoreCase("unlink")) {
				if (p.getDisplayName() != p.getName()) {
					p.setDisplayName(p.getName());
					p.setPlayerListName(p.getName());
					
					Team playerTeam = TeamScoreboards.getInstance().getPlayerTeam(p);
					if (playerTeam != null) {
						p.setDisplayName(playerTeam.getColor() + playerTeam.getPrefix() + p.getName());
						p.setPlayerListName(playerTeam.getColor() + playerTeam.getPrefix() + p.getName());
					}
					
					this.twitch.removePlayerInPlayerTwitchname(p.getName());
					
					if (p.isOp()) {
						if (!TeamScoreboards.getInstance().isPlayerInAdmins(p))
							TeamScoreboards.getInstance().removePlayerFromAdmins(p);
					}
					
					p.setScoreboard(TeamScoreboards.getInstance().getScoreboard());
					
					p.sendMessage(ChatColor.GREEN + "Le lien à bien été supprimé !");
				}
			}
		}
		
		return true;
	}
	
	// Private
	//=============================
	/**
	 * Créer le message lors de la validation du lien avec le compte twitch
	 * 
	 * @return
	 */
	private TextComponent createTwitchLinkMessage(String twitchUsername) {
		TextComponent mainMsg = new TextComponent("Le lien avec ");
		mainMsg.setColor(ChatColor.GREEN);
		TextComponent link = new TextComponent("twitch.tv/" + twitchUsername);
		link.setBold(true);
		link.setUnderlined(true);
		link.setColor(ChatColor.DARK_PURPLE);
		link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click me !").create()));
		link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://twitch.tv/" + twitchUsername));
		mainMsg.addExtra(link);
		mainMsg.addExtra(" à bien été établie !");
		
		return mainMsg;
	}
	
}
