package com.lelann.stand.commands;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;

import com.lelann.factions.utils.ChatUtils;

public class CommandsManager {
	private static CommandsManager instance;
	public static CommandsManager getInstance(){
		return instance;
	}
	
	private Map<String, AbstractCommand> commands;
	
	public void addCommand(AbstractCommand command, String... aliases){
		commands.put(command.getName().toLowerCase(), command);
		for(final String aliase : aliases){
			commands.put(aliase, command);
		}
	}
	public AbstractCommand get(String name){
		return commands.get(name.toLowerCase());
	}
	public void useCommand(CommandSender sender, String[] args){
		if(args.length == 0){
			useCommand(sender, new String[]{"help"});
			return;
		}
		
		AbstractCommand command = commands.get(args[0].toLowerCase());
		if(command == null){
			ChatUtils.sendMessage(sender, AbstractCommand.PREFIX + "%red%Mauvaise utilisation. Pour en savoir plus /stand help !");
		} else if(!command.hasPermission(sender)){
			ChatUtils.sendMessage(sender, "%red%Vous n'avez pas la permission d'utiliser cette commande !");
		} else {
			String[] otherArgs = new String[args.length - 1];
			for(int i=1;i<args.length;i++)
				otherArgs[i-1] = args[i];
			command.runCommand(sender, otherArgs);
		}
	}
	public CommandsManager(){
		instance = this;
		commands = new LinkedHashMap<String, AbstractCommand>();
		
		addCommand(new Place(), "p");
		addCommand(new SetName());
		
		addCommand(new See());
		addCommand(new Add());
		addCommand(new Buy(), "b");
		addCommand(new Pnj());
		
		addCommand(new Ap());
		
		addCommand(new Help(), "h", "aide");
	}
}
