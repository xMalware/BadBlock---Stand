package com.lelann.stand.objects;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import com.lelann.stand.StandPlugin;
import com.lelann.stand.abstracts.StandObject;
import com.lelann.stand.inventories.APTopGUI;
import com.lelann.stand.inventories.CategoryGUI;
import com.lelann.stand.inventories.abstracts.Categories;
import com.lelann.stand.inventories.abstracts.InventoryManager;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.AttributeInstance;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.GenericAttributes;

public class ApPNJ extends StandObject {

	@Getter@Setter private String name;
	@Getter@Setter private Location location;
	
	@Getter@Setter private String guiTitle;
	@Getter@Setter private Villager entity;
	
	@Getter@Setter private int professionId = 2;
	
	public ApPNJ(String name, String guiTitle, Location loc, int professionId){
		this.name = name;
		this.location = loc;
		this.guiTitle = guiTitle;
		this.professionId = professionId;
	}
	
	public void save() {
		Categories.saveApPnj(name, guiTitle, location, professionId);
	}

	public Entity createEntity(){
		Villager v = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
		v.setAdult();
		v.setCanPickupItems(false);
		v.setCustomName(name);
		v.setCustomNameVisible(true);
		v.setRemoveWhenFarAway(false);
		v.setProfession(Profession.getProfession(professionId));
		
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
	
	public String locationToString() {
		return getLocation().getBlockX() + " " 
				+ getLocation().getBlockY() + " "
				+ getLocation().getBlockZ();
	}
	
	public void openGui(Player p) {
		APTopGUI gui = InventoryManager.getApGui(p);
		gui.showBefore();
	}

	@SuppressWarnings("deprecation")
	public int getColor() {
		switch (getEntity().getProfession().getId()) {
			case 0: return 12;
			case 1: return 0;
			case 2: return 10;
			case 3: return 15;
			case 4: return 8;
	
			default: return 0;
		}
	}

	public void changeProfession() {
		professionId++;
		if(professionId >= Profession.values().length)
			professionId = 0;
		getEntity().setProfession(Profession.getProfession(professionId));
	}

	public void delete() {
//		StandPlugin.get().getManager().getPnjs().remove(getEntity().getUniqueId());
//		StandPlugin.get().getManager().savePnjs();
		StandPlugin.get().getAPManager().removeApPnj();
		Categories.removeApPnj();
		getEntity().remove();
	}
	
}
