package com.lelann.stand.inventories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.ItemStack;

import com.lelann.factions.api.FactionChunk;
import com.lelann.factions.database.Callback;
import com.lelann.factions.utils.ItemUtils;
import com.lelann.stand.StandPlugin;
import com.lelann.stand.inventories.abstracts.AbstractInventory;
import com.lelann.stand.inventories.abstracts.ClickableItem;
import com.lelann.stand.inventories.abstracts.InventoryManager;
import com.lelann.stand.listeners.ChatListener;
import com.lelann.stand.objects.APOffer;
import com.lelann.stand.objects.StandFaction;
import com.lelann.stand.objects.StandPlayer;
import com.lelann.stand.selection.APUtils;

public class APGui extends AbstractInventory {

	private StandPlayer viewer;
	private StandFaction faction;
	
	private boolean selecting = false;
	private Callback<FactionChunk> selectCallback;
	private List<FactionChunk> selected = new ArrayList<>();
	
	private Map<Integer, FactionChunk> APsBySlot = new HashMap<>();
	
	public APGui(StandPlayer viewer, StandFaction faction) {
		super("&7Vos APs", 
				(faction.getFaction().getApChunkNumber() + (faction.getFaction().getApChunkNumber() % 9 == 0 ? 0 : 9 - (faction.getFaction().getApChunkNumber() % 9))) + 9,
						viewer.getPlayer());
		this.faction = faction;
		this.viewer = viewer;
		
		//setup();
	}
	
	public APGui(StandPlayer viewer, StandFaction faction, boolean selecting) {
		super("&7Séléctionnez un AP", 
				(faction.getFaction().getApChunkNumber() + (faction.getFaction().getApChunkNumber() % 9 == 0 ? 0 : 9 - (faction.getFaction().getApChunkNumber() % 9))) + 9,
						viewer.getPlayer());
		this.faction = faction;
		this.viewer = viewer;
		this.selecting = selecting;
		
		//setup();
	}
	
	private void setup() {
		InventoryManager.addGui(this);
		setBottomBar(false, true);
		
		printAPs();
	}
	
	private boolean hasAps = false;
	private void printAPs() {
		int slot = 0;
		if(faction.getFaction().getApChunkNumber() == 0) {
			hasAps = false;
			return;
		}
		hasAps = true;
		for(FactionChunk ap : faction.getFaction().getAPs(viewer.getPlayer().getWorld().getName())) {
			if(!ap.isAp()) continue;
			if(!selecting) {
				if(!ap.isOnSale()) {
					ItemStack apStack = ItemUtils.create("&6" + ap.toString(), new String[] {
							"&7> &bClic droit&7 pour vendre votre AP",
							"&7> &bClic gauche&7 pour vous tp à votre AP"}, Material.OBSIDIAN);
					addClickable(slot, new ClickableItem(apStack, new ItemAction() {
						
						@Override
						public void run(Player p, ItemStack clicked, int slot, InventoryAction action) {
							if(action == InventoryAction.PICKUP_HALF) {
								sellAp(slot);
							} else {
								tpToAp(slot, p);
							}
							
						}
					}));
				} else {
					ItemStack apStack = ItemUtils.create("&6" + ap.toString() + "&7 [&cEN VENTE&7]", new String[] {
							"&7> &bClic droit&7 pour annuler la vente",
							"&7> &bClic gauche&7 pour vous tp à votre AP"}, Material.OBSIDIAN);
					addClickable(slot, new ClickableItem(apStack, new ItemAction() {
						
						@Override
						public void run(Player p, ItemStack clicked, int slot, InventoryAction action) {
							if(action == InventoryAction.PICKUP_HALF) {
								unsellAp(slot);
							} else {
								tpToAp(slot, p);
							}
							
						}
					}));
				}
			} else {
				if(selected != null && selected.contains(ap)) continue;
				ItemStack apStack = ItemUtils.create("&6" + ap.toString(), new String[] {
						"&7> &bClique&7 pour séléctionner cet AP"}, Material.OBSIDIAN);
				addClickable(slot, new ClickableItem(apStack, new ItemAction() {
					
					@Override
					public void run(Player p, ItemStack clicked, int slot, InventoryAction action) {
						goBack();
						selectCallback.call(null, APsBySlot.get(slot));
						selectCallback = null;
						selecting = false;
					}
				}));
			}
			APsBySlot.put(slot, ap);
			slot++;
		}
	}
	
	private void sellAp(int slot) {
		FactionChunk current = APsBySlot.get(slot);
		if(current == null) {
			System.out.println("CHUNK IS NULL AT SLOT " + slot);
			return;
		}
		if(current.isOnSale()) {
			sendFMessage("&cCet AP est déjà en vente !");
			return;
		}
		getPlayer().closeInventory();
		sendFMessage("&eVeuillez indiquer un prix de vente. Ex: 10000 | Pour annuler, tapez simplement 'annuler'.");
		ChatListener.waitForCommand.put(getPlayer().getUniqueId(), new Callback<String>() {
			
			@Override
			public void call(Throwable t, String result) {
				
				if(result.equalsIgnoreCase("annuler")) {
					sendFMessage("&eAnnulé.");
					InventoryManager.restore(APGui.this);
					show();
					return;
				}
				
				if(!validNumber(result)) {
					sendFMessage("&cPrix invalide.");
					return;
				}
				
				int price = getNumber(result);
				
				if(price < MIN_PRICE) {
					sendFMessage("&cPrix invalide. Montant minimum: " + MIN_PRICE + "$");
					return;
				}
				
				if(price > MAX_PRICE) {
					sendFMessage("&cPrix invalide. Montant maximal: " + MAX_PRICE + "$");
					return;
				}
				
				StandPlugin.get().sellAp(faction.getFaction(), current, price);
				faction.getFaction().sendMessage("&c" + getPlayer().getName() + "&e a mis en vente l'AP en &c" + current.toString() + "&e pour &c" + price + "$ &e!");
			}
		});
	}
	
	private void unsellAp(int slot) {
		FactionChunk current = APsBySlot.get(slot);
		if(current == null) return;
		if(!current.isOnSale()) return;
		APOffer toRevok = faction.getOffer(current);
		getPlayer().closeInventory();
		StandPlugin.get().unsellAp(faction.getFaction(), toRevok);
		faction.getFaction().sendMessage("&c" + getPlayer().getName() + "&e a annulé la mise en vente de l'AP en &c" + current.toString() + "&e !");
	}
	
	private void tpToAp(int slot, Player p) {
		FactionChunk current = APsBySlot.get(slot);
		if(current == null) return;
		
		p.teleport(APUtils.getTpLoc(p.getWorld().getName(), current.getX(), current.getZ()));
		sendFMessage("&eTéléporté !");
	}
	
	@Override
	public void show() {
		setup();
		if(!hasAps) {
			//goBack();
			sendNMessage(getPlayer(), "&cVous n'avez pas d'APs.");
			return;
		}
		super.show();
	}
	
	public void openSelect(AbstractInventory back, List<FactionChunk> selected, Callback<FactionChunk> done) {
		this.selectCallback = done;
		this.selected = selected;
		back.displayGui(this);
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
