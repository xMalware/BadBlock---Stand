package com.lelann.stand.commands;

import org.bukkit.command.CommandSender;

import com.lelann.factions.utils.ChatUtils;

public class Help extends AbstractCommand {
	
	public Help() {
		super("help", "stand.play.help", "&c&l>&7 /stand help", "&7Affiche l'aide", "/stand help", null);
	}

	@Override
	public void runCommand(CommandSender sender, String[] args) {
		
		ChatUtils.sendMessage(sender,
				"&8&l«&b&l-&8&l»&m----------&8[&7Aide&8]&m----------&f&8&l«&b&l-&8&l»&b");
		
		this.sendHelp(sender);
		CommandsManager.getInstance().get("add").sendHelp(sender);
		CommandsManager.getInstance().get("setname").sendHelp(sender);
		CommandsManager.getInstance().get("see").sendHelp(sender);
		CommandsManager.getInstance().get("place").sendHelp(sender);
		
		if(hasPermission(sender, "stand.admin.*")){
			CommandsManager.getInstance().get("pnj").sendHelp(sender);	
		}
		
		ChatUtils.sendMessage(sender,
				"&8&l«&b&l-&8&l»&m-------------------------&f&8&l«&b&l-&8&l»&b");
		
	}
}
