package com.lelann.stand.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

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
	
	@EventHandler
	public void guiClick(InventoryClickEvent e) {
		AbstractInventory gui = InventoryManager.getClickedGui(e);
		if(gui == null) {
			System.out.println("gui is null");
			return;
		}
		
		boolean cancel = gui.callClickEvent(e);
		e.setCancelled(cancel);
		
		//System.out.println("èh: " + e.getClickedInventory().getTitle() + ", item: " + e.getCurrentItem());
		
		ClickableItem item = gui.getItem(e.getSlot());
		
		//System.out.println("test: " + (item == null ? "ITEM NULL" : item.getSlot()) + " clicked for clickableitem in gui");
		
		if(item != null && item.getAction() != null && (gui.isActive() || e.getSlot() >= gui.getSize()-9))
			item.getAction().run((Player) e.getWhoClicked(), e.getCurrentItem(), e.getSlot(), e.getAction());
		
	}
	
	@EventHandler 
	public void guiClose(InventoryCloseEvent e) {
		AbstractInventory gui = InventoryManager.getClickedGui(e);
		if(gui == null) return;
		
		gui.callCloseEvent(e);
	}
	
}
