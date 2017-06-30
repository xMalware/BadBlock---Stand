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
import com.lelann.stand.inventories.CategoryGUI;
import com.lelann.stand.inventories.abstracts.InventoryManager;
import com.lelann.stand.objects.CategoryPNJ;

/**
 * On gère ici les interactions avec les villageois customs
 * @author Coco
 *
 */
public class CategoryPNJListener extends StandObject implements Listener {

	@EventHandler
	public void entityInteract(PlayerInteractEntityEvent e) {
		if(e.getRightClicked().getType() != EntityType.VILLAGER) return;
		if(!StandPlugin.get().getManager().isPnj(e.getRightClicked())) return;
		
		e.setCancelled(true);
		
		CategoryPNJ pnj = StandPlugin.get().getManager().getPnj(e.getRightClicked());
		if(pnj == null) return;
		
		if(e.getPlayer().isSneaking() && (e.getPlayer().isOp() || e.getPlayer().hasPermission("stand.admin.pnj.edit"))) {
			CategoryGUI gui = InventoryManager.getCategoryGui(e.getPlayer(), pnj);
			gui.openEdit(e.getPlayer());
		} else {
			pnj.openGui(e.getPlayer());
		}
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
			if(protect(entity) && !StandPlugin.get().getManager().getPnjs().containsKey(entity.getUniqueId())){
				entity.remove();
			}
		}
	}
	
	private boolean protect(Entity e){
		return e.hasMetadata("standPNJ");
	}
	
}
