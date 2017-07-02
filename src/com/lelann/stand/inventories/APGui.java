package com.lelann.stand.inventories;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.ItemStack;

import com.lelann.factions.Main;
import com.lelann.factions.api.FactionChunk;
import com.lelann.factions.utils.ItemUtils;
import com.lelann.stand.inventories.abstracts.AbstractInventory;
import com.lelann.stand.inventories.abstracts.ClickableItem;
import com.lelann.stand.inventories.abstracts.InventoryManager;
import com.lelann.stand.objects.StandFaction;
import com.lelann.stand.objects.StandPlayer;

public class APGui extends AbstractInventory {

	private StandPlayer viewer;
	private StandFaction faction;
	
	public APGui(StandPlayer viewer, StandFaction faction) {
		super("&7Vos APs", 
				18,
						viewer.getPlayer());
		this.faction = faction;
		this.viewer = viewer;
		
		System.out.println("size: " + (faction.getFaction().getApChunkNumber() + faction.getFaction().getApChunkNumber() % 9 == 0 ? 0 : 9 - (faction.getFaction().getApChunkNumber() % 9) + 9));
		
		setup();
	}
	
	private void setup() {
		InventoryManager.addGui(this);
		setBottomBar(false, true);
		
		printAPs();
	}
	
	private void printAPs() {
		int slot = 0;
		for(FactionChunk ap : faction.getFaction().getAPs(viewer.getPlayer().getWorld().getName())) {
			ItemStack apStack = ItemUtils.create("&6" + ap.toString(), new String[] {
					"&7> &bClic droit&7 pour vendre votre AP",
					"&7> &bClic gauche&7 pour vous tp à votre AP"}, Material.OBSIDIAN);
			addClickable(slot, new ClickableItem(apStack, new ItemAction() {
				
				@Override
				public void run(Player p, ItemStack clicked, int slot, InventoryAction action) {
					p.sendMessage("Vente/Tp à faire (:");
					
				}
			}));
		}
	}
	
	/* ----------- UNUSED ------------ */

	@Override
	public boolean onClick(Player p, ItemStack clicked, ItemStack cursor, int slot, InventoryAction action,
			ClickType clickType, SlotType slotType) {
		return true;
	}

	@Override
	public void onClose(Player p) { }
	
}
