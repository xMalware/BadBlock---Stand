package com.lelann.stand.inventories.abstracts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.lelann.stand.inventories.APTopGUI;
import com.lelann.stand.inventories.CategoryGUI;
import com.lelann.stand.inventories.LoadingGUI;
import com.lelann.stand.inventories.abstracts.AbstractInventory.ItemAction;
import com.lelann.stand.objects.ApPNJ;
import com.lelann.stand.objects.CategoryPNJ;

public class InventoryManager {
	
	public static List<AbstractInventory> guis = new ArrayList<>();
	public static Map<AbstractInventory, List<ClickableItem>> clickables = new HashMap<>();
	
	public static List<AbstractInventory> restorables = new ArrayList<>();
	public static Map<AbstractInventory, List<ClickableItem>> restorabelsClickables = new HashMap<>();
	
	public static Map<CategoryPNJ, CategoryGUI> categoryGuis = new HashMap<>();
	
	public static LoadingGUI getLoadingGui(Player p) {
		return new LoadingGUI("&7Chargement", p);
	}
	
	public static void addGui(AbstractInventory gui) {
		guis.add(gui);
		clickables.put(gui, new ArrayList<>());
	}
	
	public static void removeGui(AbstractInventory gui) {
		restorables.add(gui);
		guis.remove(gui);
		restorabelsClickables.put(gui, clickables.get(gui));
		clickables.remove(gui);
		
	}

	public static void registerItem(AbstractInventory gui, ItemStack item, ItemAction action) {
		if(clickables.get(gui) == null) clickables.put(gui, new ArrayList<>());
		ClickableItem clickable = new ClickableItem(item, action);
		List<ClickableItem> items = clickables.get(gui);
		items.add(clickable);
		clickables.put(gui, items);
	}

	public static void unregisterItem(AbstractInventory gui, ClickableItem item) {
		List<ClickableItem> items = clickables.get(gui);
		items.remove(item);
		clickables.put(gui, items);
	}
	
	public static void unregisterItems(AbstractInventory gui) {
		clickables.remove(gui);
	}
	
	public static void registerItem(AbstractInventory gui, ClickableItem item) {
		if(clickables.get(gui) == null) clickables.put(gui, new ArrayList<>());
		List<ClickableItem> items = clickables.get(gui);
		items.add(item);
		clickables.put(gui, items);
	}

	public static CategoryGUI getCategoryGui(Player p, CategoryPNJ pnj) {
		return new CategoryGUI(pnj.getGuiTitle(), pnj, p);
	}
	
	public static AbstractInventory getClickedGui(InventoryClickEvent event) {
		for(AbstractInventory gui : InventoryManager.guis) {
			if(gui.isSimilar(event.getClickedInventory()) && gui.getPlayer().getName().equals(event.getWhoClicked().getName())) {
				return gui;
			}
		}
		return null;
	}
	
	public static AbstractInventory getClickedGui(InventoryCloseEvent event) {
		for(AbstractInventory gui : InventoryManager.guis) {
			if(gui.isSimilar(event.getInventory()) && gui.getPlayer().getName().equals(event.getPlayer().getName())) {
				return gui;
			}
		}
		return null;
	}

	public static AbstractInventory getGui(Inventory inv, Player player) {
		for(AbstractInventory gui : InventoryManager.guis) {
			if(gui.isSimilar(inv) && gui.getPlayer().getName().equals(player.getName())) {
				return gui;
			}
		}
		return null;
	}

	public static void restore(AbstractInventory gui) {
		if(restorables.contains(gui)) {
			guis.add(gui);
			restorables.remove(gui);
		}
		if(restorabelsClickables.containsKey(gui)) {
			clickables.put(gui, restorabelsClickables.get(gui));
			restorabelsClickables.remove(gui);
		}
	}

	public static APTopGUI getApGui(Player p) {
		return new APTopGUI(p);
	}
	
}
