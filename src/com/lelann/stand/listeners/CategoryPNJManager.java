package com.lelann.stand.listeners;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;

import com.lelann.factions.runnables.FRunnable;
import com.lelann.stand.inventories.abstracts.InventoryManager;
import com.lelann.stand.objects.CategoryPNJ;

import lombok.Getter;

public class CategoryPNJManager {

	@Getter
	private Map<UUID, CategoryPNJ> pnjs = new HashMap<>();
	@Getter
	private Map<String, CategoryPNJ> identifiers = new HashMap<>();
	
	public CategoryPNJManager(List<CategoryPNJ> pnjs) {
		for(final CategoryPNJ pnj : pnjs) {
			add(pnj);
		}
		
		loadCategoryGuis();
	}
	
	public void reload() {
		loadCategoryGuis();
	}
	
	/**
	 * Charge les inventaires
	 */
	private void loadCategoryGuis() {
		for(final CategoryPNJ pnj : pnjs.values()) {
			InventoryManager.createCategoryGui(pnj.getGuiTitle(), pnj);
		}
	}
	
	public void add(CategoryPNJ pnj) {
		final Villager entity = (Villager) pnj.createEntity();
		pnjs.put(entity.getUniqueId(), pnj);
		identifiers.put(pnj.getIdentifier(), pnj);
		
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
	
	public void savePnjs(){
		for(CategoryPNJ pnj : pnjs.values()){
			pnj.save();
		}
	}

	public boolean isPnj(Entity entity) {
		return pnjs.containsKey(entity.getUniqueId());
	}

	public CategoryPNJ getPnj(Entity e) {
		return pnjs.get(e.getUniqueId());
	}
	
	public CategoryPNJ getPnj(String identifier) {
		return identifiers.get(identifier);
	}
	
}
