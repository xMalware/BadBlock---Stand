package com.lelann.stand.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.lelann.factions.Main;
import com.lelann.factions.api.Faction;
import com.lelann.factions.api.FactionChunk;
import com.lelann.factions.api.FactionPlayer;
import com.lelann.stand.StandPlugin;
import com.lelann.stand.inventories.APGui;
import com.lelann.stand.objects.APOffer;
import com.lelann.stand.objects.StandFaction;
import com.lelann.stand.objects.StandPlayer;

public class Ap extends AbstractCommand {

	public Ap() {
		super("_[-ap-]_", "stand.play.ap");
	}

	@Override
	public void runCommand(CommandSender sender, String[] args) {
		if(!(sender instanceof Player)){
			sendMessage(sender, NO_CONSOLE);
			return;
		}
		
		//
		// /stand ap list -> gui
		// /stand ap sell x,z prix
		// /stand ap buy prix
		//
		
		StandPlayer player = getPlayer(sender);
		FactionPlayer p = Main.getInstance().getPlayersManager().getPlayer(sender);
		Faction f = p.getFaction();
		StandFaction faction = StandPlugin.get().getStandFaction(f);
		
		if(args.length == 0) return;
		
		if(args[0].equalsIgnoreCase("list")) {
			
			APGui gui = new APGui(player, faction);
			gui.show();
			
		} else if(args[0].equalsIgnoreCase("sell")) {
			
			if(faction.getOffers().size() > 0) {
				sendFMessage(sender, "&cVous devez attendre la vente de votre AP avant d'en vendre un nouveau !");
				return;
			}
			
			if(args.length < 3) {
				return;
			}
			
			FactionChunk chunk = Main.getInstance().getChunksManager(p.getPlayer().getLocation().getWorld()).getFactionChunk(args[1]);
			
			int price = Integer.parseInt(args[2]);
			
			if(price < 2000) {
				sendFMessage(sender, "&cPrix invalide. Montant minimum: 2000");
				return;
			}
			
			if(price > 300000) {
				sendFMessage(sender, "&cPrix invalide. Montant maxima: 300000");
				return;
			}
			
			APOffer offer = new APOffer(f, chunk, price);
			faction.addOffer(offer);
			faction.save();
			
		} else if(args[0].equalsIgnoreCase("buy")) {
			
		}
		
	}
	
}
