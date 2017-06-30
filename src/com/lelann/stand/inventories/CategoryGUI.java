package com.lelann.stand.inventories;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.ItemStack;

import com.lelann.factions.utils.ChatUtils;
import com.lelann.factions.utils.ItemUtils;
import com.lelann.stand.StandPlugin;
import com.lelann.stand.inventories.abstracts.AbstractInventory;
import com.lelann.stand.inventories.abstracts.ClickableItem;
import com.lelann.stand.inventories.abstracts.InventoryManager;
import com.lelann.stand.inventories.abstracts.Tops;
import com.lelann.stand.objects.CategoryPNJ;

public class CategoryGUI extends AbstractInventory {

	private CategoryPNJ from;
	private boolean editing = false;
	
	public CategoryGUI(String title, CategoryPNJ from, Player viewer) {
		super(title, viewer);
		this.from = from;
		setup();
	}

	private void setup() {
		InventoryManager.addGui(this);
		setBottomBar(true, true);
		if(from.getItems().length > 0) {
			int slot = 0;
			for(ItemStack item : from.getItems()) {
				if(item == null || item.getType() == Material.AIR) {
					slot++;
					continue;
				}
				ItemAction openVendorsForItem = new ItemAction() {
					@Override
					public void run(Player p, ItemStack clicked, int slot, InventoryAction action) {
						Tops.openTopGui(p, clicked);
					}
				};
				ItemStack display = ItemUtils.create(new String[] {"", "&a> &7Voir les meilleurs prix de vente"}, item);
				addClickable(display, slot, openVendorsForItem);
				slot++;
			}
		}
	}
	
	@Override
	public boolean onClick(Player p, ItemStack clicked, ItemStack cursor, int slot, InventoryAction action,
			ClickType clickType, SlotType slotType) {
		return isActive() || slot > (getSize()-9);
	}

	@Override
	public void onClose(Player p) {
		if(editing) {
			saveInventory(p);
		}
	}
	
	private void saveInventory(Player p) {
		editing = false;
		setActive(true);
		
		from.setItems(getContents());
		
		StandPlugin.get().getManager().savePnjs();
		ChatUtils.sendMessage(p, "&aPNJ modifié !");
		
		resetBottomBar(true, true);
		
		p.closeInventory();
		StandPlugin.get().getManager().reload();
	}
	
	public ItemStack[] getContents() {
		ItemStack[] stacks = new ItemStack[9 * 5];
		for(int slot = 0; slot < getSize()-9; slot++) {
			stacks[slot] = getInventory().getItem(slot);
		}
		return stacks;
	}

	public void openEdit(Player player) {
		if(editing) {
			ChatUtils.sendMessage(player, "&cQuelqu'un est déjà en train de modifier ce pnj !");
			return;
		}
		editing = true;
		setActive(false);
		
		editBottomBar(0, new ClickableItem(getSeparator(), null));
		
		ItemStack validate = ItemUtils.create("&aValider", Material.STAINED_GLASS_PANE, 13);
		editBottomBar(4, new ClickableItem(validate, new ItemAction() {
			@Override
			public void run(Player p, ItemStack clicked, int slot, InventoryAction action) {
				saveInventory(p);
			}
		}));
		
		ItemStack editColor = ItemUtils.create("&7Modifier le type de pnj", new String[] {"&7Cliquez pour changer la profession du pnj !"}, Material.WOOL, from.getColor());
		editBottomBar(2, new ClickableItem(editColor, new ItemAction() {
			
			@Override
			public void run(Player p, ItemStack clicked, int slot, InventoryAction action) {
				from.changeProfession();
				getInventory().setItem(getSize() - 9 + 2, ItemUtils.create("&7Modifier le type de pnj", new String[] {"&7Cliquez pour changer la profession du pnj !"}, Material.WOOL, from.getColor()));
			}
		}));
		
		show();
	}

	public void resetAll() {
		//InventoryManager.unregisterItems(this);
		InventoryManager.removeGui(this);
	}

}
