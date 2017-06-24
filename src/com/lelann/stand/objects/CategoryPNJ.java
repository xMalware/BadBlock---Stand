package com.lelann.stand.objects;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import com.lelann.stand.StandPlugin;
import com.lelann.stand.abstracts.StandObject;
import com.lelann.stand.inventories.CategoryGUI;
import com.lelann.stand.inventories.abstracts.Categories;
import com.lelann.stand.inventories.abstracts.InventoryManager;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.AttributeInstance;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.GenericAttributes;

public class CategoryPNJ extends StandObject {

	@Getter@Setter private String name;
	@Getter@Setter private ItemStack[] items;
	@Getter@Setter private Location location;
	
	@Getter@Setter private String guiTitle;
	@Getter@Setter private String identifier;
	@Getter@Setter private Entity entity;
	
	public CategoryPNJ(String identifier, String name, String guiTitle, Location loc, List<ItemStack> items){
		this.name = name;
		this.location = loc;
		this.items = items.toArray(new ItemStack[items.size()]);
		this.guiTitle = guiTitle;
		this.identifier = identifier;
	}
	
	public void save() {
		Categories.saveCategory(identifier, name, guiTitle, location, items);
	}

	public Entity createEntity(){
		Villager v = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
		v.setAdult();
		v.setCanPickupItems(false);
		v.setCustomName(name);
		v.setCustomNameVisible(true);
		v.setRemoveWhenFarAway(false);
		
		v.setMetadata("standPNJ", new FixedMetadataValue(StandPlugin.get(), ""));
		
		try {
			AttributeInstance attributes = ((EntityInsentient)((CraftLivingEntity) v).getHandle()).getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);
			attributes.setValue(0.0f);
		} catch (IllegalArgumentException | SecurityException e) {
			e.printStackTrace();
		}
		
		this.entity = v;
		return v;
	}
	
	public void openGui(Player p) {
		CategoryGUI gui = InventoryManager.getCategoryGui(this);
		gui.show(p);
	}
	
}
