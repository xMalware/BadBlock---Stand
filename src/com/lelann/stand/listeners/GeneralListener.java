package com.lelann.stand.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.lelann.factions.api.events.FactionCreatedEvent;
import com.lelann.factions.api.events.FactionDeleteEvent;
import com.lelann.stand.Requests;
import com.lelann.stand.StandPlugin;
import com.lelann.stand.abstracts.StandObject;
import com.lelann.stand.objects.StandFaction;
import com.lelann.stand.objects.StandPlayer;
import com.lelann.stand.objects.StandPlayer.StandAction;

public class GeneralListener extends StandObject implements Listener {
	@EventHandler
	public void onJoin(PlayerJoinEvent e){
		StandPlugin.get().connect(e.getPlayer());
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e){
		StandPlayer player = getPlayer(e.getPlayer());
		player.setAction(StandAction.NOTHING);
		
		Requests.savePlayer(player);
	}
	
	@EventHandler
	public void factionCreated(FactionCreatedEvent e) {
		StandFaction newFaction = new StandFaction(e.getFaction());
		StandPlugin.get().addStandFaction(newFaction);
	}
	
	@EventHandler
	public void factionDeleted(FactionDeleteEvent e) {
		StandPlugin.get().removeStandFaction(e.getFaction());
	}
}
