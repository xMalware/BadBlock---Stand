package com.lelann.stand.inventories.abstracts;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
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
import com.lelann.stand.abstracts.StandObject;

import lombok.Getter;
import lombok.Setter;

/**
 * Classe qui gère les inventaires avec simplicité
 * @author Coco
 *
 */

public abstract class AbstractInventory extends StandObject {

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
	private Map<Integer, ClickableItem> itemsSlot = new HashMap<>();
	
	@Getter@Setter
	private AbstractInventory back;
	
	@Getter@Setter
	private ItemStack separator;
	
	public AbstractInventory(String title) {
		this.title = title;
		this.size = 54;
		defaultSeparator();
		build();
	}
	
	public AbstractInventory(String title, int size) {
		this.title = title;
		this.size = size;
		defaultSeparator();
		build();
	}
	
	protected void build() {
		this.gui = Bukkit.createInventory(null, this.size, ChatUtils.colorReplace(this.title));
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
		gui.back = this;
		gui.show(player);
	}
	
	public boolean goBack() {
		if(back != null) {
			back.show(player);
			back = null;
		}
		else player.closeInventory();
		return true;
	}
	
	public boolean isSimilar(Inventory inv) {
		return inv == gui || (size == inv.getSize() && ChatUtils.colorDelete(title).equals(ChatUtils.colorDelete(inv.getTitle())));
	}

	public boolean callClickEvent(InventoryClickEvent e) {
		return onClick((Player) e.getWhoClicked(), e.getCurrentItem(), e.getCursor(), e.getSlot(), e.getAction(), e.getClick(), e.getSlotType());
	}
	
	public void callCloseEvent(InventoryCloseEvent e) {
		onClose((Player) e.getPlayer());
	}
	
	public void addClickable(ItemStack item, int slot, ItemAction action) {
		ClickableItem it = new ClickableItem(item, action);
		InventoryManager.registerItem(this, it);
		itemsSlot.put(slot, it);
		gui.setItem(slot, item);
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
		ClickableItem clickable = new ClickableItem(item, action);
		itemsSlot.put(slot, clickable);
		InventoryManager.registerItem(this, clickable);
	}

	public void addClickable(int slot, ClickableItem item) {
		InventoryManager.registerItem(this, item);
		item.setSlot(slot);
		itemsSlot.put(slot, item);
		gui.setItem(slot, item.getItem());
	}
	
	public void removeClickable(ClickableItem item) {
		InventoryManager.unregisterItem(this, item);
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
		for(int slot = 0; slot < 9; slot++) {
			if(slot == 0 && !chest) {
				addSeparator(startSlot+slot);
			} else if(slot == 8 && !retour) {
				addSeparator(startSlot+slot);
			} else if(slot > 0 && slot < 8) {
				addSeparator(startSlot+slot);
			}
		}
	}
	
	public void editBottomBar(int slot, ClickableItem edited) {
		slot = (size-9) + slot;
		/*List<ClickableItem> list = InventoryManager.clickables.get(this);
		for(int pos = 0; pos < list.size(); pos++) {
			for(int s = 0; s < size; s++) {
				if(gui.getItem(s) != null && gui.getItem(s).isSimilar(list.get(pos).getItem())) {
					list.set(pos, edited);
					gui.setItem(slot, edited.getItem());
					//addClickable(slot, item);
					break;
				}
			}
		}*/
		
		InventoryManager.unregisterItem(this, getItem(slot));
		addClickable(slot, edited);
		
		//InventoryManager.clickables.put(this, list);
	}
	
	public void resetBottomBar(boolean chest, boolean retour) {
		for(int slot = 0; slot < 8; slot++) {
			slot = size-9+slot;
			removeClickable(getClickable(slot));
		}
		setBottomBar(chest, retour);
	}
	
	public ClickableItem getItem(ItemStack item) {
		if(items.containsKey(item)) return items.get(item);
		else {
			for(List<ClickableItem> clickables : InventoryManager.clickables.values()) {
				for(ClickableItem clickable : clickables) {
					//System.out.println("comparing " + clickable.getItem().getType() + " with " + item.getType() + " ..");
					if(clickable.getItem().isSimilar(item)) {
						items.put(item, clickable);
						System.out.println("founded item clickable");
						return clickable;
					}
				}
			}
			System.out.println("not found item clickable :c");
			return null;
		}
	}
	
	public ClickableItem getItem(int slot) {
		return itemsSlot.get(slot);
	}
	
	public ClickableItem getClickable(int slot) {
		ItemStack stack = getInventory().getItem(slot);
		if(stack == null) { System.out.println("item is null at slot " + slot); return null; }
		return getItem(slot);
	}
	
	public ClickableItem getBarClickable(int slot) {
		slot = (size-9) + slot;
		ItemStack stack = getInventory().getItem(slot);
		if(stack == null) { System.out.println("item is null at slot " + slot); return null; }
		return getItem(stack);
	}
	
	public Inventory getInventory() {
		return gui;
	}
	
	public void update() {
		//System.out.println("updating inv");
		//Packets.updateInventory(getPlayer(), getInventory());
	}
	
	public ItemStack[] getContents(int lines) {
		int size = 9 * lines;
		ItemStack[] total = new ItemStack[size];
		for(int slot = 0; slot < size; slot++) {
			total[slot] = getInventory().getItem(slot);
		}
		return total;
	}
	
	public abstract boolean onClick(Player p, ItemStack clicked, ItemStack cursor, int slot, InventoryAction action, ClickType clickType, SlotType slotType);
	
	public abstract void onClose(Player p);
	
	public abstract class ItemAction {
		
		public abstract void run(Player p, ItemStack clicked, int slot, InventoryAction action);
		
	}
}
