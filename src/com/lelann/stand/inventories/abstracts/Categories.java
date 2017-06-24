package com.lelann.stand.inventories.abstracts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import com.lelann.factions.utils.ConfigUtils;
import com.lelann.factions.utils.ItemUtils;
import com.lelann.stand.objects.CategoryPNJ;

public class Categories {

	private static File config;
	private static FileConfiguration configuration;
	
	public static List<CategoryPNJ> loadCategories(File c) {
		setupConfiguration(c);
		
		List<CategoryPNJ> temp = new ArrayList<>();
		
		String section = "Pnjs";
		ConfigurationSection cs = configuration.getConfigurationSection(section);
		for(String id : cs.getKeys(false)) {
			ConfigurationSection current = cs.getConfigurationSection(id);
			String name = current.getString("Name");
			String title = current.getString("InventoryTitle");
			Location loc = ConfigUtils.locationFromConfig(current);
			List<ItemStack> items = ItemUtils.getItemsFromStrings(current.getStringList("Items"));
			temp.add(new CategoryPNJ(id, name, title, loc, items));
		}
		
		return temp;
	}
	
	public static void saveCategory(String identifier, String name, String title, Location loc, ItemStack[] items) {
		configuration.set("Pnjs." + identifier, null);
		String path = "Pnjs." + identifier;
		ConfigurationSection current = configuration.getConfigurationSection(path);
		
		configuration.set(path + ".Name", name);
		configuration.set(path + ".InventoryTitle", title);
		ConfigUtils.itemsToConfig(current, items);
		ConfigUtils.locationToConfig(current, loc);
		
		save();
		reloadPnjs();
	}
	
	private static void reloadPnjs() {
		//TODO: reload les inventaires
	}
	
	private static void save() {
		try {
			configuration.save(config);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void setupConfiguration(File c) {
		try {	
			config = c;
			configuration = YamlConfiguration.loadConfiguration(config);
			configuration.save(config);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
