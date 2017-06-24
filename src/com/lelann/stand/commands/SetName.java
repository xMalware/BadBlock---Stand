package com.lelann.stand.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.lelann.factions.utils.ChatUtils;
import com.lelann.factions.utils.StringUtils;
import com.lelann.stand.Requests;
import com.lelann.stand.objects.StandPlayer;

public class SetName extends AbstractCommand {
	public SetName() {
		super("setname", "stand.play.setname", "%gold%/stand setname %red%name", "%gold%Change le nom de votre stand pour %red%name", "/stand setname <name>", null);
	}

	@Override
	public void runCommand(CommandSender sender, String[] args) {
		if(!(sender instanceof Player)){
			sendMessage(sender, NO_CONSOLE);
			return;
		}
		
		if(args.length == 0){
			sendHelp(sender); return;
		}
		
		String name = ChatUtils.colorReplace(StringUtils.join(args, " "));
		if(name.length() > 32){
			sendMessage(sender, "%red%Le nom de stand est trop grand, il ne doit pas dépasser 32 caractères.");
		} else {
			StandPlayer player = getPlayer(sender);
			player.setStandName(name);
			Requests.savePlayer(player);
			sendMessage(sender, "%yellow%Le nom de votre stand a bien été changé !");
		}
	}
}