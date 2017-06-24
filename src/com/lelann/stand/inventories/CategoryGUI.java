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
import com.lelann.stand.inventories.abstracts.Tops;
import com.lelann.stand.objects.CategoryPNJ;

public class CategoryGUI extends AbstractInventory {

	private CategoryPNJ from;
	private boolean editing = false;
	
	public CategoryGUI(String title, CategoryPNJ from) {
		super(title);
		this.from = from;
		setup();
	}

	private void setup() {
		setBottomBar(true, true);
		
		for(ItemStack item : from.getItems()) {
			ItemAction openVendorsForItem = new ItemAction() {
				@Override
				public void run(Player p, ItemStack clicked, int slot, InventoryAction action) {
					Tops.openTopGui(p, clicked);
				}
			};
			ItemStack display = ItemUtils.create(new String[] {"", "&a> &7Voir les meilleurs prix de vente"}, item);
			addClickable(display, openVendorsForItem);
		}
	}
	
	@Override
	public boolean onClick(Player p, ItemStack clicked, ItemStack cursor, int slot, InventoryAction action,
			ClickType clickType, SlotType slotType) {
		return isActive();
	}

	@Override
	public void onClose(Player p) {
		if(editing) {
			editing = false;
			setActive(true);
			
			p.sendMessage("&cModification annulée");
		}
	}
	
	private void saveInventory(Player p) {
		editing = false;
		setActive(true);
		
		from.setItems(getInventory().getContents());
		
		StandPlugin.get().getManager().savePnjs();
		p.sendMessage("&aPNJ modifié !");
		
		p.closeInventory();
	}

	public void openEdit(Player player) {
		if(editing) {
			ChatUtils.sendMessage(player, "&cQuelqu'un est déjà en train de modifier ce pnj !");
			return;
		}
		setActive(false);
		ItemStack validate = ItemUtils.create("&aValider", Material.STAINED_GLASS_PANE, 13);
		editBottomBar(4, new ClickableItem(validate, new ItemAction() {
			@Override
			public void run(Player p, ItemStack clicked, int slot, InventoryAction action) {
				saveInventory(p);
			}
		}));
	}

}
