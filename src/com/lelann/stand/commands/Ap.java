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
import com.lelann.stand.objects.APOffer;
import com.lelann.stand.objects.APRequest;
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
		
		StandPlayer player = getPlayer(sender);
		FactionPlayer p = Main.getInstance().getPlayersManager().getPlayer(sender);
		Faction f = p.getFaction();
		StandFaction faction = StandPlugin.get().getStandFaction(f);
		
		if(args.length == 0) return;
		
		if(args[0].equalsIgnoreCase("removerequest")) {
			
			String total = args[1];
			String[] parts = total.split(":");
			int initial = getNumber(parts[0]);
			int wanted = getNumber(parts[1]);
			int gived = getNumber(parts[2]);
			
			APRequest found = null;
			for(APRequest req : faction.getRequests()) {
				if(req.getInitialAmount() == initial
						&& req.getWantedAmount() == wanted
						&& req.getGived() == gived) {
					found = req;
					break;
				}
			}
			
			if(found == null) return;
			
			found.setWantedAmount(0);
			faction.removeRequest(found);
			faction.save();
			
			sendFMessage(sender, "&eDemande supprim�e !");
			
		} else if(args[0].equalsIgnoreCase("list")) {
			
			faction.openGui(player);
			
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
			
			if(price < MIN_PRICE) {
				sendFMessage(sender, "&cPrix invalide. Montant minimum: " + MIN_PRICE);
				return;
			}
			
			if(price > MAX_PRICE) {
				sendFMessage(sender, "&cPrix invalide. Montant maximal: " + MAX_PRICE);
				return;
			}
			
			StandPlugin.get().sellAp(f, chunk, price);
			f.sendMessage("&c" + sender.getName() + "&e a mis en vente l'AP en &c" + chunk.toString() + "&e pour &c" + price + "$ &e!");
			
		} else if(args[0].equalsIgnoreCase("buy")) {
			
			if(args.length >= 2 && args[1].equalsIgnoreCase("list")) {
				faction.sendList(sender);
				return;
			}
			
			if(!validNumber(args[1])) {
				return;
			}
			
			int price = getNumber(args[1]);
			int amount = getNumber(args[2]);
			
			int totalRequested = 0;
			for(APRequest req : faction.getRequests()) {
				totalRequested += req.getWantedAmount();
			}
			
			if(faction.getFaction().getApChunkNumber() >= 4) {
				sendFMessage(sender, "&cVous avez d�ja accumul� plus de 4 APs ! Vous ne pouvez pas en demander plus !");
				return;
			}
			
			if(faction.getFaction().getApChunkNumber() + totalRequested >= 4) {
				sendFMessage(sender, "&cVous �tes d�j� en train de demander plusieurs APs vous faisant atteindre la limite de 4 APs.");
				return;
			}
			
			if(faction.getFaction().getApChunkNumber() + totalRequested + amount > 4) {
				sendFMessage(sender, "&cVous allez d�passer la limite de 4 APs par achat en faisant cela !");
				return;
			}
			
			if(faction.getFaction().getCapital() < (price * amount)) {
				sendFMessage(sender, "&cVous n'avez pas assez de capital pour pouvoir demander " + amount + " APs !");
				return;
			}
			
			APRequest request = new APRequest(f, price, amount);
			faction.addRequest(request);
			
			f.sendMessage("&c" + sender.getName() + "&e a cr�e une demande pour tenter d'obtenir &c" + amount + "&e APs � &c" + price + "$ &echacun !");
			
		} else if(args[0].equalsIgnoreCase("sendtitleaponsale")) {
			String c = args[1];
			FactionChunk chunk = Main.getInstance().getChunksManager(player.getPlayer().getWorld()).getFactionChunk(c);
			if(chunk == null) return;
			Faction owner = chunk.getOwner();
			
			if(owner == null) {
				return;
			}
			
			StandFaction current = StandPlugin.get().getStandFaction(owner);
			
			APOffer offer = current.getOffer(chunk);
			if(offer == null) {
				return;
			}
			
			String title = "&7AP en vente !";
			String subtitle = "&7Faction: " + MoveListener.color(f, owner) + owner.getName() + " &7Prix: &b" + offer.getPrice() + "$";
			
			new Title(title, subtitle, 10, 60, 10).send((Player) sender);
		}
		
	}
	
}
