package com.lelann.stand.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;

import com.lelann.factions.runnables.FRunnable;
import com.lelann.stand.abstracts.StandObject;
import com.lelann.stand.objects.ApPNJ;

import lombok.Getter;

public class ApPNJManager extends StandObject {

	@Getter
	private ApPNJ pnj = null;
	
	public ApPNJManager(ApPNJ pnj) {
		add(pnj);
	}
	
	public void add(ApPNJ pnj) {
		this.pnj = pnj;
		final Villager entity = (Villager) pnj.createEntity();
		
		for(Entity e : entity.getNearbyEntities(1.0f, 1.0f, 1.0f)){
			if(entity.getCustomName().equals(e.getCustomName()))
				e.remove();
		}
		
		new FRunnable(40L){
			@Override
			public void run(){
				if(entity == null || entity.isDead()) {
					cancel(); return;
				}
				Location loc = pnj.getLocation().clone();
				loc.setYaw(entity.getLocation().getYaw());
				loc.setPitch(entity.getLocation().getPitch());
				
				entity.teleport(loc);
			}
		}.start();
	}
	
	public void savePnj() {
		pnj.save();
	}

	public boolean isPnj(Entity entity) {
		return pnj != null && pnj.getEntity().getUniqueId().equals(entity.getUniqueId());
	}
	
	public ApPNJ getApPNJ() {
		return pnj;
	}
	
}
