package com.lelann.stand.inventories;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.lelann.factions.utils.ItemUtils;
import com.lelann.stand.StandPlugin;
import com.lelann.stand.inventories.abstracts.AbstractInventory;
import com.lelann.stand.inventories.abstracts.InventoryManager;

public class LoadingGUI extends AbstractInventory {

	private ItemStack[][] states = new ItemStack[6][9];
	private int currentState = 0;
	private BukkitTask task;
	
	public LoadingGUI(String title, Player p) {
		super(title, 9, p);
		setupAnimation();
		animateLoading();
		InventoryManager.addGui(this);
	}
	
	private void setupAnimation() {
		ItemStack white = ItemUtils.create("Chargement...", Material.STAINED_GLASS_PANE, 0);
		ItemStack blue = ItemUtils.create("Chargement...", Material.STAINED_GLASS_PANE, 11);
		ItemStack lightblue = ItemUtils.create("Chargement...", Material.STAINED_GLASS_PANE, 3);
		for(int state = 0; state < states.length; state++) {
			for(int currentItemPos = 0; currentItemPos < states[state].length; currentItemPos++) {
				if(state == 0) {
					if(currentItemPos == 4) {
						states[state][currentItemPos] = blue;
					} else {
						states[state][currentItemPos] = white;
					}
				} else if(state == 1) {
					if(currentItemPos == 3 || currentItemPos == 5) {
						states[state][currentItemPos] = blue;
					} else if(currentItemPos == 4) {
						states[state][currentItemPos] = lightblue;
					} else {
						states[state][currentItemPos] = white;
					}
				} else if(state == 2) {
					if(currentItemPos == 2 || currentItemPos == 6) {
						states[state][currentItemPos] = blue;
					} else if(currentItemPos == 3 || currentItemPos == 5) {
						states[state][currentItemPos] = lightblue;
					} else {
						states[state][currentItemPos] = white;
					}
				} else if(state == 3) {
					if(currentItemPos == 1 || currentItemPos == 7) {
						states[state][currentItemPos] = blue;
					} else if(currentItemPos == 2 || currentItemPos == 6) {
						states[state][currentItemPos] = lightblue;
					} else {
						states[state][currentItemPos] = white;
					}
				} else if(state == 4) {
					if(currentItemPos == 0 || currentItemPos == 8) {
						states[state][currentItemPos] = blue;
					} else if(currentItemPos == 1 || currentItemPos == 7) {
						states[state][currentItemPos] = lightblue;
					} else {
						states[state][currentItemPos] = white;
					}
				} else if(state == 5) {
					if(currentItemPos == 0 || currentItemPos == 8) {
						states[state][currentItemPos] = lightblue;
					} else {
						states[state][currentItemPos] = white;
					}
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void animateLoading() {
		task = Bukkit.getServer().getScheduler().runTaskTimer(StandPlugin.get(), new BukkitRunnable() {
			
			@Override
			public void run() {
				printState();
				currentState++;
				if(currentState >= states.length) {
					currentState = 0;
				}
			}
			
		}, 20L, 20L);
	}
	
	public void stopAnimation() {
		task.cancel();
		task = null;
	}
	
	private void printState() {
		for(int pos = 0; pos < states[currentState].length; pos++) {
			getInventory().setItem(pos, states[currentState][pos]);
		}
	}
	
	@Override
	public boolean onClick(Player p, ItemStack clicked, ItemStack cursor, int slot, InventoryAction action,
			ClickType clickType, SlotType slotType) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void onClose(Player p) {
		// TODO Auto-generated method stub
		
	}

	
}
