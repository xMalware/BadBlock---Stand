package com.lelann.stand.inventories;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.ItemStack;

import com.lelann.stand.inventories.abstracts.AbstractInventory;

public class StandGUI extends AbstractInventory {

	//TODO REFAIRE LE GUI
	
	public StandGUI(String title, int size) {
		super(title, size);
	}

	@Override
	public boolean onClick(Player p, ItemStack clicked, ItemStack cursor, int slot, InventoryAction action,
			ClickType clickType, SlotType slotType) {
		return true;
	}

	@Override
	public void onClose(Player p) {
		
	}

}
