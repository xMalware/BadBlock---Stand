package com.lelann.stand.job;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.lelann.factions.api.FactionPlayer;
import com.lelann.factions.api.jobs.objects.JobListener;
import com.lelann.factions.api.jobs.objects.JobManager;
import com.lelann.stand.events.ItemBoughtEvent;

public class SellerJobListener extends JobListener {

	public SellerJobListener() {
		super(SellerJob.class);
	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		FactionPlayer p = getPlayersManager().getPlayer(e.getPlayer());
		if(e.getMessage().contains("vendeur")) {
			if(!p.is(SellerJob.class)) {
				p.assignNewJob(JobManager.getJob(SellerJob.class));
			}
		}
	}
	
	@EventHandler
	public void onBuy(ItemBoughtEvent e) {
		FactionPlayer owner = getFactionPlayer(e.getOffer().getOwner());
		if(owner.is(SellerJob.class)) {
			owner.getJobValues(SellerJob.class).gainXp(2);
		}
	}
	
}
