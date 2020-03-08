package com.toguy.giwit.commands.tab_completion;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.command.*;

public class SwapTabCompletion implements TabCompleter {

	@Override
    public @Nullable List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if (args.length == 1){
        	List<String> arguments = new ArrayList<String>();
        	
        	arguments.add("players");
        	arguments.add("inventory");
        	arguments.add("set");
        	
        	return arguments;
        }

        return null;
    }
	
}
