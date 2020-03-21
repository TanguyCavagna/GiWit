package com.toguy.giwit.commands.tab_completion;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class UHCAdministrationTabCompletion implements TabCompleter {
	
	/**
	 * Retourne la liste des arguments de commande disponibles
	 */
	@Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> arguments = new ArrayList<String>();

		if (args.length == 2) {
        	arguments.add("world");
        } else if (args.length == 1) {
        	arguments.add("scenarios");
        	arguments.add("remake");
        	arguments.add("shrink");
        	arguments.add("start");
        }

		return arguments;
    }
	
}
