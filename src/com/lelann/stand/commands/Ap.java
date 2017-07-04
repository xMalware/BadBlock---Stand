package com.lelann.stand.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.lelann.factions.Main;
import com.lelann.factions.api.Faction;
import com.lelann.factions.api.FactionChunk;
import com.lelann.factions.api.FactionPlayer;
import com.lelann.factions.listeners.MoveListener;
import com.lelann.factions.utils.Title;
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
			
			faction.openGui(player);
			
		} else if(args[0].equalsIgnoreCase("sell")) {
			
			System.out.println(faction.getOffers().size());
			
			if(faction.getOffers().size() > 0) {
				sendFMessage(sender, "&cVous devez attendre la vente de votre AP avant d'en vendre un nouveau !");
				return;
			}
			
			if(args.length < 3) {
				return;
			}
			
			FactionChunk chunk = Main.getInstance().getChunksManager(p.getPlayer().getLocation().getWorld()).getFactionChunk(args[1]);
			
			int price = Integer.parseInt(args[2]);
			
			if(price < MIN_PRICE) {
				sendFMessage(sender, "&cPrix invalide. Montant minimum: 2000");
				return;
			}
			
			if(price > MAX_PRICE) {
				sendFMessage(sender, "&cPrix invalide. Montant maximal: 300000");
				return;
			}
			
			StandPlugin.get().sellAp(f, chunk, price);
			f.sendMessage("&c" + sender.getName() + "&e a mis en vente l'AP en &c" + chunk.toString() + "&e pour &c" + price + "$ &e!");
			
		} else if(args[0].equalsIgnoreCase("buy")) {
			
			
			
		} else if(args[0].equalsIgnoreCase("sendtitleaponsale")) {
			String c = args[1];
			FactionChunk chunk = Main.getInstance().getChunksManager(player.getPlayer().getWorld()).getFactionChunk(c);
			if(chunk == null) return;
			Faction owner = chunk.getOwner();
			
			if(owner == null) {
				System.out.println("owner null");
				return;
			}
			
			StandFaction current = StandPlugin.get().getStandFaction(owner);
			APOffer offer = current.getOffer(chunk);
			if(offer == null) {
				System.out.println("offer null");
				return;
			}
			String title = "&7AP en vente !";
			String subtitle = "&7Faction: " + MoveListener.color(f, owner) + owner.getName() + " &7Prix: &b" + offer.getPrice() + "$";
			
			new Title(title, subtitle, 10, 60, 10).send((Player) sender);
		}
		
	}
	
}
