package com.lelann.stand.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.lelann.factions.Main;
import com.lelann.factions.api.FactionPlayer;
import com.lelann.stand.StandPlugin;
import com.lelann.stand.objects.StandPlayer;

import lombok.Getter;

public class StandListener implements Listener {
	@Getter private static StandListener instance;
	
	private Map<UUID, UUID> pnjs;
	
	public void add(Location loc, Player p, final StandPlayer player){
		final ArmorStand entity = player.place(p.getName(), null, loc);
		this.pnjs.put(entity.getUniqueId(), p.getUniqueId());
	}
	
	public void add(final StandPlayer player, boolean remove){
		FactionPlayer fp = Main.getInstance().getPlayersManager().getPlayer(player.getUniqueId());
		
		if(fp == null || player == null) return;
		
		final ArmorStand entity = player.place(fp.getLastUsername(), null, player.getStandLoc());
		if(!remove)
			this.pnjs.put(entity.getUniqueId(), player.getUniqueId());
		
		for(Entity e : entity.getNearbyEntities(1.0f, 1.0f, 1.0f)){
			if(entity.getCustomName().equals(e.getCustomName()) && !entity.getUniqueId().equals(e.getUniqueId()))
				e.remove();
		}
		
		if(remove) entity.remove();
	}
	
	public StandListener(List<StandPlayer> players){
		instance = this;
		
		this.pnjs = new HashMap<UUID, UUID>();
		
		for(final StandPlayer player : players){
			add(player, player.getStandRemove() < System.currentTimeMillis());
		}
	}
	
	private boolean protect(Entity e){
		return pnjs.containsKey(e.getUniqueId());
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent e){
		if(e.getEntity().getType() == EntityType.ARMOR_STAND && protect(e.getEntity())){
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true) 
	public void onClickStand(PlayerArmorStandManipulateEvent e){
		if(protect(e.getRightClicked())){
			e.setCancelled(true);
			
			UUID uniqueId = pnjs.get(e.getRightClicked().getUniqueId());
			StandPlayer owner = StandPlugin.get().getPlayer(uniqueId);
			
			owner.openStand(e.getPlayer());
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onJoin(PlayerJoinEvent e){
		StandPlayer player = StandPlugin.get().getPlayer(e);
		if(player.getStandLoc() != null) player.setStandLoc(null);
		
		List<UUID> toRemove = new ArrayList<>();
		
		if(pnjs.values().contains(e.getPlayer().getUniqueId())){
			for(Map.Entry<UUID, UUID> pnj : pnjs.entrySet()){
				if(pnj.getValue().equals(e.getPlayer().getUniqueId())){
					for(Entity ent : e.getPlayer().getNearbyEntities(15.0d, 15.0d, 15.0d)){
						if(ent.getUniqueId().equals(pnj.getKey()) || player.getStandName().equals(ent.getCustomName()))
							ent.remove();
						else if(ent.getType() == EntityType.ARMOR_STAND && !pnjs.containsKey(ent.getUniqueId()))
							ent.remove();
					}
					
					toRemove.add(pnj.getKey());
				}
			}
		}
		
		for(UUID uniqueId : toRemove){
			pnjs.remove(uniqueId);
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e){
		StandPlayer player = StandPlugin.get().getPlayer(e);
		if(player.getStandLoc() != null) {
			e.getPlayer().teleport(player.getStandLoc());
		}
	}
}
