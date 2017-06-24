package com.lelann.stand.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.lelann.factions.Main;
import com.lelann.factions.api.FactionPlayer;
import com.lelann.stand.objects.StandPlayer;

public class See extends AbstractCommand {
	public See() {
		super("see", "stand.play.see", "%gold%/stand see %red%(player)", "%gold%Voir le stand de %red%name", "/stand see <player>", null);
	}

	@Override
	public void runCommand(CommandSender sender, String[] args) {
		if(!(sender instanceof Player)){
			sendMessage(sender, NO_CONSOLE);
			return;
		}
		
		Player p = (Player) sender;
		FactionPlayer player = null;

		if(args.length == 0){
			player = Main.getInstance().getPlayersManager().getPlayer(sender);
		} else {
			String name = args[0];
			player = Main.getInstance().getPlayersManager().getPlayer(args[0]);
			if(player == null){
				sendMessage(sender, "%red%Le joueur '" + name + "' est introuvable !"); return;
			}
		}
		
		StandPlayer sPlayer = getPlayer(player.getUniqueId());
		sPlayer.openStand(p);
	}
}