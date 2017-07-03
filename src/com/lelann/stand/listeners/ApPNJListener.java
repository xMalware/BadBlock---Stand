package com.lelann.stand.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import com.lelann.stand.StandPlugin;
import com.lelann.stand.abstracts.StandObject;
import com.lelann.stand.objects.ApPNJ;

/**
 * On gère ici les interactions avec le villageois AP.
 * @author Coco
 *
 */
public class ApPNJListener extends StandObject implements Listener {

	@EventHandler
	public void entityInteract(PlayerInteractEntityEvent e) {
		if(e.getRightClicked().getType() != EntityType.VILLAGER) return;
		if(!StandPlugin.get().getAPManager().isPnj(e.getRightClicked())) return;
		
		e.setCancelled(true);
		
		ApPNJ pnj = StandPlugin.get().getAPManager().getPnj();
		if(pnj == null) return;
		
		pnj.openGui(e.getPlayer());
		
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent e){
		if(e.getEntity().getType() == EntityType.VILLAGER && protect(e.getEntity())){
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onLoadChunk(ChunkLoadEvent e){
		for(Entity entity : e.getChunk().getEntities()){
			if(protect(entity) && !StandPlugin.get().getAPManager().getPnj().getEntity().getUniqueId().equals(entity.getUniqueId())) {
				entity.remove();
			}
		}
	}
	
	private boolean protect(Entity e){
		return e.hasMetadata("standPNJ");
	}
	
}
