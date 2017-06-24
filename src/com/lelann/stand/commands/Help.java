package com.lelann.stand.commands;

import org.bukkit.command.CommandSender;

public class Help extends AbstractCommand {
	public Help() {
		super("help", "stand.play.help", "%gold%/stand help", "%gold%Affiche l'aide du plugin Stand", "/stand add <type>", null);
	}

	@Override
	public void runCommand(CommandSender sender, String[] args) {
		CommandsManager.getInstance().get("add").sendHelp(sender);
		CommandsManager.getInstance().get("setname").sendHelp(sender);
		CommandsManager.getInstance().get("see").sendHelp(sender);
		CommandsManager.getInstance().get("place").sendHelp(sender);
		
		
		if(hasPermission(sender, "stand.admin.*")){
			CommandsManager.getInstance().get("pnj").sendHelp(sender);	
		}
	}
}
