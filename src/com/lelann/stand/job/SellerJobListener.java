package com.lelann.stand.job;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.lelann.factions.api.FactionPlayer;
import com.lelann.factions.api.jobs.JobEvent;
import com.lelann.factions.api.jobs.JobManager;
import com.lelann.stand.events.ItemBoughtEvent;

public class SellerJobListener extends JobEvent {

	public SellerJobListener() {
		super(SellerJob.class);
	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		FactionPlayer p = getPlayersManager().getPlayer(e.getPlayer());
		if(e.getMessage().contains("vendeur")) {
			if(!p.is("Vendeur")) {
				p.assignJob(JobManager.getJob("Vendeur"));
			}
		}
	}
	
	@EventHandler
	public void onBuy(ItemBoughtEvent e) {
		FactionPlayer owner = getFactionPlayer(e.getOffer().getOwner());
		if(owner.is("Vendeur")) {
			owner.getJobValues("Vendeur").gainXp(2);
		}
	}
	
}
