package com.lelann.stand.inventories.abstracts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

import com.lelann.stand.inventories.CategoryGUI;
import com.lelann.stand.inventories.LoadingGUI;
import com.lelann.stand.inventories.abstracts.AbstractInventory.ItemAction;
import com.lelann.stand.objects.CategoryPNJ;

import lombok.Getter;

public class InventoryManager {

	public static List<AbstractInventory> guis = new ArrayList<>();
	public static Map<AbstractInventory, List<ClickableItem>> clickables = new HashMap<>();
	
	public static Map<CategoryPNJ, CategoryGUI> categoryGuis = new HashMap<>();
	
	@Getter
	private static LoadingGUI loadingGui;
	
	static {
		createLoadingGui();
	}
	
	public static void createLoadingGui() {
		if(loadingGui != null) return;
		else {
			loadingGui = new LoadingGUI("&7Chargement | Veuillez patienter");
		}
	}
	
	public static void addGui(AbstractInventory gui) {
		guis.add(gui);
	}
	
	public static void removeGui(AbstractInventory gui) {
		guis.remove(gui);
	}

	public static void registerItem(AbstractInventory gui, ItemStack item, ItemAction action) {
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
	
	public static void registerItem(AbstractInventory gui, ClickableItem item) {
		List<ClickableItem> items = clickables.get(gui);
		items.add(item);
		clickables.put(gui, items);
	}
	
	public static CategoryGUI createCategoryGui(String title, CategoryPNJ from) {
		if(categoryGuis.containsKey(from)) return categoryGuis.get(from);
		else {
			CategoryGUI created = new CategoryGUI(title, from);
			categoryGuis.put(from, created);
			return created;
		}
	}

	public static CategoryGUI getCategoryGui(CategoryPNJ pnj) {
		return categoryGuis.get(pnj);
	}
	
}
