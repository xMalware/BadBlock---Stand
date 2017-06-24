package com.lelann.stand.listeners;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import com.lelann.stand.abstracts.StandObject;
import com.lelann.stand.inventories.abstracts.AbstractInventory;
import com.lelann.stand.inventories.abstracts.ClickableItem;
import com.lelann.stand.inventories.abstracts.InventoryManager;

/**
 * On gère les clics sur les inventaires custom (AbstractInventory)
 * @author Coco
 *
 */
public class GuiListener extends StandObject implements Listener {

	private Map<Inventory, AbstractInventory> guisByInvs = new HashMap<>();
	
	@EventHandler
	public void guiClick(InventoryClickEvent e) {
		AbstractInventory gui = getGui(e.getClickedInventory());
		if(gui == null) return;
		
		boolean cancel = gui.callClickEvent(e);
		e.setCancelled(cancel);
		
		ClickableItem item = gui.getItem(e.getCurrentItem());
		if(item != null && item.getAction() != null && (gui.isActive() || e.getSlot() >= gui.getSize()-9))
			item.getAction().run((Player) e.getWhoClicked(), e.getCurrentItem(), e.getSlot(), e.getAction());
		
	}
	
	@EventHandler 
	public void guiClose(InventoryCloseEvent e) {
		AbstractInventory gui = getGui(e.getInventory());
		if(gui == null) return;
		
		gui.callCloseEvent(e);
	}
	
	private AbstractInventory getGui(Inventory inv) {
		if(guisByInvs.containsKey(inv)) return guisByInvs.get(inv);
		else {
			for(AbstractInventory gui : InventoryManager.guis) {
				if(gui.isSimilar(inv)) {
					guisByInvs.put(inv, gui);
					return gui;
				}
			}
			return null;
		}
	}
	
}
