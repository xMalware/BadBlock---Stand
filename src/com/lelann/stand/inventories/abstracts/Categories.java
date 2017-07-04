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
import com.lelann.stand.objects.ApPNJ;
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
			if(id.equalsIgnoreCase("ap")) continue;
			ConfigurationSection current = cs.getConfigurationSection(id);
			String name = current.getString("Name");
			String title = current.getString("InventoryTitle");
			int professionId = current.getInt("Profession");
			Location loc = ConfigUtils.locationFromConfig(current);
			List<ItemStack> items = ItemUtils.getItemsFromStrings(current.getStringList("Items"));
			temp.add(new CategoryPNJ(id, name, title, loc, items, professionId));
		}
		
		return temp;
	}
	
	public static void saveCategory(String identifier, String name, String title, Location loc, ItemStack[] items, int professionId) {
		configuration.set("Pnjs." + identifier, null);
		String path = "Pnjs." + identifier;
		//ConfigurationSection current = configuration.getConfigurationSection(path);
		
		configuration.set(path + ".Name", name);
		configuration.set(path + ".InventoryTitle", title);
		configuration.set(path + ".Profession", professionId);
		ConfigUtils.itemsToConfig(configuration, path, items);
		ConfigUtils.locationToConfig(configuration, path, loc);
		
		save();
	}
	
	public static void removeCategory(String identifier) {
		configuration.set("Pnjs." + identifier, null);
		save();
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

	public static void saveApPnj(String name, String title, Location location, int professionId) {
		configuration.set("Pnjs.AP", null);
		String path = "Pnjs.AP";
		//ConfigurationSection current = configuration.getConfigurationSection(path);
		
		configuration.set(path + ".Name", name);
		configuration.set(path + ".InventoryTitle", title);
		configuration.set(path + ".Profession", professionId);
		ConfigUtils.locationToConfig(configuration, path, location);
		
		save();
	}

	public static void removeApPnj() {
		configuration.set("Pnjs.AP", null);
		save();
	}

	public static ApPNJ loadAP() {
		String path = "Pnjs.AP";
		ConfigurationSection current = configuration.getConfigurationSection(path);
		if(current != null) {
			String name = current.getString("Name");
			String title = current.getString("InventoryTitle");
			int professionId = current.getInt("Profession");
			Location loc = ConfigUtils.locationFromConfig(current);
			return new ApPNJ(name, title, loc, professionId);
		}
		return null;
	}
	
}
