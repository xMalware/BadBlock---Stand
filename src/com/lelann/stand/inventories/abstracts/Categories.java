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
		
		if(cs == null) {
			cs = configuration.createSection(section);
			return new ArrayList<>();
			/*cs.set("Blocs.Name", "&b> &7Des blocs &b<");
			cs.set("Blocs.InventoryTitle", "&7Checkez les blocs !");
			cs.set("Blocs.Location", null);
			cs.set("Blocs.Items", Arrays.asList("STONE:0"));*/
		}
		
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
		configuration.set("Pnjs", null);
		configuration.set("Pnjs." + identifier, null);
		String path = "Pnjs." + identifier;
		//ConfigurationSection current = configuration.getConfigurationSection(path);
		
		configuration.set(path + ".Name", name);
		configuration.set(path + ".InventoryTitle", title);
		ConfigUtils.itemsToConfig(configuration, path, items);
		ConfigUtils.locationToConfig(configuration, path, loc);
		
		save();
		reloadPnjs();
	}
	
	private static void reloadPnjs() {
		//TODO: reload les inventaires
	}
	
	private static void save() {
		try {
			System.out.println("saving config");
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
