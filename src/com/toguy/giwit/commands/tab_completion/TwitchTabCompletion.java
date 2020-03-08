package com.toguy.giwit.commands.tab_completion;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class TwitchTabCompletion implements TabCompleter {

	@Override
    public @Nullable List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> arguments = new ArrayList<String>();
		
		if (args.length == 1) {
			arguments.add("link");
			arguments.add("unlink");
		}
		
		return arguments;
	}
	
}
