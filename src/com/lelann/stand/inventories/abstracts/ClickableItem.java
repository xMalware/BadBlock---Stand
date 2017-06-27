package com.lelann.stand.inventories.abstracts;

import org.bukkit.inventory.ItemStack;

import com.lelann.stand.inventories.abstracts.AbstractInventory.ItemAction;

import lombok.Getter;
import lombok.Setter;

public class ClickableItem {
	
	@Getter@Setter
	private ItemStack item;
	@Getter@Setter
	private ItemAction action;
	@Getter@Setter
	private int slot = -1;
	
	public ClickableItem(ItemStack item, ItemAction action) {
		this.item = item;
		this.action = action;
	}
	
	public ClickableItem(ItemStack item, ItemAction action, int slot) {
		this.item = item;
		this.action = action;
		this.slot = slot;
	}

	public ClickableItem update(ItemStack item) {
		this.item = item;
		return this;
	}
	
}
