package com.lelann.stand.inventories.abstracts;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.lelann.factions.utils.ChatUtils;
import com.lelann.factions.utils.ItemUtils;
import com.lelann.stand.StandPlugin;

import lombok.Getter;
import lombok.Setter;

/**
 * Classe qui gère les inventaires avec simplicité
 * @author Coco
 *
 */

public abstract class AbstractInventory {

	@Getter@Setter
	private String title;
	
	@Getter@Setter
	private Player player;
	
	private Inventory gui;
	@Getter@Setter
	private int size;
	
	@Getter@Setter
	private boolean isActive = true;
	
	private Map<ItemStack, ClickableItem> items = new HashMap<>();
	
	@Getter@Setter
	private AbstractInventory back;
	
	@Getter@Setter
	private ItemStack separator;
	
	public AbstractInventory(String title) {
		this.title = title;
		this.size = 54;
		defaultSeparator();
	}
	
	public AbstractInventory(String title, int size) {
		this.title = title;
		this.size = size;
		defaultSeparator();
	}
	
	public void defaultSeparator() {
		ItemStack sepa = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 11);
		ItemMeta meta = sepa.getItemMeta();
		meta.setDisplayName(" ");
		sepa.setItemMeta(meta);
		setSeparator(sepa);
	}
	
	public void show(Player p) {
		setPlayer(p);
		p.openInventory(gui);
	}
	
	public void displayGui(AbstractInventory gui) {
		back = this;
		gui.show(player);
	}
	
	public boolean goBack() {
		if(back != null)
			back.show(player);
		else player.closeInventory();
		return true;
	}
	
	public boolean isSimilar(Inventory inv) {
		return inv == gui || size == inv.getSize() && title.equals(inv.getTitle());
	}

	public boolean callClickEvent(InventoryClickEvent e) {
		return onClick((Player) e.getWhoClicked(), e.getCurrentItem(), e.getCursor(), e.getSlot(), e.getAction(), e.getClick(), e.getSlotType());
	}
	
	public void callCloseEvent(InventoryCloseEvent e) {
		onClose((Player) e.getPlayer());
	}
	
	public void addClickable(ItemStack item, int slot, ItemAction action) {
		InventoryManager.registerItem(this, item, action);
		gui.setItem(slot, item);
	}
	
	public void addClickable(ItemStack item, ItemAction action) {
		InventoryManager.registerItem(this, item, action);
		gui.addItem(item);
	}
	
	public void addSeparator(int slot) {
//		getInventory().setItem(slot, separator);
		addClickable(separator, slot, null);
	}
	
	public void addClickable(String name, String[] desc, Material type, int amount, int data, int slot, ItemAction action) {
		ItemStack item = new ItemStack(type, amount, (byte) data);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatUtils.colorReplace(name));
		meta.setLore(Arrays.asList(ChatUtils.colorReplace(desc)));
		item.setItemMeta(meta);
		InventoryManager.registerItem(this, item, action);
	}

	public void addClickable(int slot, ClickableItem item) {
		InventoryManager.registerItem(this, item);
		gui.setItem(slot, item.getItem());
	}
	
	/**
	 * Récupération de la barre de navigation du bas
	 * @param chest Si il peut y avoir un chest (1er item)
	 * @param retour S'il peut y avoir un bouton retour (dernier item)
	 */
	public void setBottomBar(boolean chest, boolean retour) {
		if(chest) {
			addClickable(ItemUtils.create("&7Votre stand", Material.CHEST), size-9, new ItemAction() {
				
				@Override
				public void run(Player p, ItemStack clicked, int slot, InventoryAction action) {
					StandPlugin.get().getPlayer(p).openStand(p);
				}
			});
		}
		if(retour) {
			addClickable(ItemUtils.create("&c< Retour", Material.BARRIER), size-1, new ItemAction() {
				
				@Override
				public void run(Player p, ItemStack clicked, int slot, InventoryAction action) {
					goBack();
				}
			});
		}
		int startSlot = size-9;
		for(int slot = 0; slot < 8; slot++) {
			if(slot == 0 && !chest) {
				addSeparator(startSlot+slot);
			} else if(slot == 8 && !retour) {
				addSeparator(startSlot+slot);
			} else {
				addSeparator(startSlot+slot);
			}
		}
	}
	
	public void editBottomBar(int slot, ClickableItem edited) {
		List<ClickableItem> list = InventoryManager.clickables.get(this);
		for(int pos = 0; pos < list.size(); pos++) {
			for(int s = 0; s < size; s++) {
				if(gui.getItem(s).equals(list.get(pos).getItem())) {
					list.set(pos, edited);
					gui.setItem(slot, edited.getItem());
					break;
				}
			}
		}
	}
	
	public ClickableItem getItem(ItemStack item) {
		if(items.containsKey(item)) return items.get(item);
		else {
			for(List<ClickableItem> clickables : InventoryManager.clickables.values()) {
				for(ClickableItem clickable : clickables) {
					if(clickable.getItem().isSimilar(item)) {
						items.put(item, clickable);
						return clickable;
					}
				}
			}
			return null;
		}
	}
	
	public ClickableItem getClickable(int slot) {
		ItemStack stack = getInventory().getItem(slot);
		if(stack == null) return null;
		return getItem(stack);
	}
	
	public Inventory getInventory() {
		return gui;
	}
	
	public abstract boolean onClick(Player p, ItemStack clicked, ItemStack cursor, int slot, InventoryAction action, ClickType clickType, SlotType slotType);
	
	public abstract void onClose(Player p);
	
	public abstract class ItemAction {
		
		public abstract void run(Player p, ItemStack clicked, int slot, InventoryAction action);
		
	}
}
