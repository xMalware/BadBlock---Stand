package com.lelann.stand.inventories;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.ItemStack;

import com.lelann.stand.inventories.abstracts.AbstractInventory;
import com.lelann.stand.inventories.abstracts.ClickableItem;
import com.lelann.stand.inventories.abstracts.InventoryManager;
import com.lelann.stand.objects.StandOffer;
import com.lelann.stand.objects.StandPlayer;

public class StandGUI extends AbstractInventory {
	
	private StandPlayer viewer;
	private StandPlayer owner;
	
	public StandGUI(Player viewer, StandPlayer owner) {
		super(owner.getStandName(), 9*6, viewer);
		this.viewer = getPlayer(viewer);
		this.owner = owner;
		setup();
	}
	
	private void setup() {
		InventoryManager.addGui(this);
		
		boolean canModify = owner.getUniqueId().equals(viewer.getUniqueId()) || viewer.getPlayer().hasPermission("stand.admin.modify");
		
		for(int i = 0; i < (owner.getOffers().size() > (9*5) ? 9*5 : owner.getOffers().size()); i++){
			StandOffer offer = owner.getOffers().get(i);
			ItemStack stack = offer.createItemStack("&7Vente à &b" + offer.getPrice() + "&7 l'unité", "",
					"&7> &bClic gauche&7 pour acheter",
					canModify ? "&7>&b Clic droit&7 pour supprimer l'offre" : "");
			ClickableItem item = new ClickableItem(stack, new ItemAction() {
				
				@Override
				public void run(Player p, ItemStack clicked, int slot, InventoryAction action) {
					if(action == InventoryAction.PICKUP_HALF) {
						if(canModify) {
							deleteOffer(slot);
						} else {
							buyItem(slot);
						}
					} else {
						buyItem(slot);
					}
				}
			});
			addClickable(i, item);
			
		}
		
		setBottomBar(false, true);
	}
	
	private void deleteOffer(int slot) {
		if(getInventory().getItem(slot) == null) {
			viewer.sendMessage("&cOffre invaide.");
			return;
		}
		
		if(slot >= owner.getOffers().size()) {
			return;
		}
		
		StandOffer offer = owner.getOffers().get(slot);
		ItemStack toGive = offer.createItemStack(offer.getAmount());
		owner.removeOffer(offer);
		
		remove(slot);
		
		viewer.getPlayer().getInventory().addItem(toGive);
		viewer.sendMessage("&aL'offre a été supprimée !");
	}
	
	private void remove(int slot) {
		getInventory().setItem(slot, null);
		ItemStack[] contents = getContents(5);
		int currentSlot = 0;
		for(int index = 0; index < contents.length; index++) {
			if(index == slot) {
				continue;
			}
			getInventory().setItem(currentSlot, contents[index]);
			currentSlot++;
		}
	}

	private void buyItem(int slot) {
		if(getInventory().getItem(slot) == null) {
			viewer.sendMessage("&cOffre invaide.");
			return;
		}
		StandOffer offer = owner.getOffers().get(slot);
		displayGui(new BuyGUI(offer, getPlayer()));
	}
	
	@Override
	public boolean onClick(Player p, ItemStack clicked, ItemStack cursor, int slot, InventoryAction action,
			ClickType clickType, SlotType slotType) {
		return true;
	}

	@Override
	public void onClose(Player p) {
		//InventoryManager.removeGui(this);
	}

}
