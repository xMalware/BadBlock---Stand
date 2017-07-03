package com.lelann.stand.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.lelann.factions.database.Callback;
import com.lelann.stand.abstracts.StandObject;

public class ChatListener extends StandObject implements Listener {

	public static Map<UUID, Callback<String>> waitForCommand = new HashMap<>();
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		if(!waitForCommand.containsKey(e.getPlayer().getUniqueId())) return;
		e.setCancelled(true);
		
		waitForCommand.get(e.getPlayer().getUniqueId()).call(null, e.getMessage());
		waitForCommand.remove(e.getPlayer().getUniqueId());
		
	}
	
}
