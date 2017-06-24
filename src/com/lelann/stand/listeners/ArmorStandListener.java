package com.lelann.stand.listeners;

import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.lelann.stand.StandPlugin;
import com.lelann.stand.objects.StandPlayer;

public class ArmorStandListener implements Listener {
//	@EventHandler public void onLoad(ChunkLoadEvent e){
//		for(Entity entity : e.getChunk().getEntities()){
//			if(entity.getType() == EntityType.ARMOR_STAND && entity.hasMetadata("standOwner")){
//				UUID uniqueId = UUID.fromString(entity.getMetadata("standOwner").get(0).asString());
//				StandPlayer player = StandPlugin.get().getPlayer(uniqueId);
//				
//				if(player.getStandRemove() < System.currentTimeMillis() || player.isValid()){
//					entity.remove();
//					player.setStandRemove(0);
//				}
//			}
//		}
//	}
	
	public void onJoin(PlayerJoinEvent e){
		StandPlugin.get().removeArmorStand(e.getPlayer().getUniqueId());
//		StandPlayer player = StandPlugin.get().getPlayer(e);
//		if(player.getStandRemove() != 0) {
//			final UUID uniqueId = e.getPlayer().getUniqueId();
//			new BukkitRunnable(){
//				@Override
//				public void run(){
//					Player player = Bukkit.getPlayer(uniqueId);
//					StandPlayer splayer = StandPlugin.get().getPlayer(player);
//
//					for(Entity entity : player.getNearbyEntities(1.0d, 1.0d, 1.0d)){
//						if(entity.getType() == EntityType.ARMOR_STAND && entity.hasMetadata("standOwner")){
//							UUID uuid = UUID.fromString(entity.getMetadata("standOwner").get(0).asString());
//							
//							if(player.getUniqueId().equals(uuid)){
//								entity.remove();
//								splayer.setStandRemove(0);
//								break;
//							}
//						}
//					}
//
//				}
//			}.runTaskLater(StandPlugin.get(), 5L);
//		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true) 
	public void onInteract(PlayerArmorStandManipulateEvent e){
		if(e.getRightClicked().hasMetadata("standOwner")){
			e.setCancelled(true);
			
			UUID uniqueId = UUID.fromString(e.getRightClicked().getMetadata("standOwner").get(0).asString());
			StandPlayer owner = StandPlugin.get().getPlayer(uniqueId);
			
			owner.openStand(e.getPlayer());
		}
	}
}
