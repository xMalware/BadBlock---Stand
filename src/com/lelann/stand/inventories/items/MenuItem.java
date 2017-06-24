package com.lelann.stand.inventories.items;

import com.lelann.stand.inventories.abstracts.AbstractInventory;
import com.lelann.stand.inventories.abstracts.ClickableItem;

public class MenuItem {

	private ClickableItem[][] items;
	
	public MenuItem(ClickableItem[][] items) {
		this.items = items;
	}
	
	public void print(AbstractInventory gui, int startSlot) {
		for(int height = 0; height < items.length; height++) {
			for(int index = 0; index < items[height].length; index++) {
				int currentSlot = startSlot + index + (height*8);
				//gui.getInventory().setItem(currentSlot, items[height][index]);
				ClickableItem current = items[height][index];
				gui.addClickable(currentSlot, current);
			}
		}
	}
	
}
