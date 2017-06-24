package com.lelann.stand.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.lelann.stand.Requests;
import com.lelann.stand.StandPlugin;
import com.lelann.stand.abstracts.StandObject;
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
}
