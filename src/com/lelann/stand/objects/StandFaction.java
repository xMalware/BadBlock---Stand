package com.lelann.stand.objects;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import com.lelann.factions.api.Faction;
import com.lelann.factions.api.FactionChunk;
import com.lelann.factions.database.Callback;
import com.lelann.stand.Requests;
import com.lelann.stand.abstracts.StandObject;
import com.lelann.stand.inventories.APGui;
import com.lelann.stand.inventories.StandGUI;
import com.lelann.stand.inventories.abstracts.AbstractInventory;
import com.lelann.stand.inventories.abstracts.InventoryManager;

import lombok.Getter;

public class StandFaction extends StandObject {

	@Getter private Faction faction;
	
	public static List<APOffer> allOffers = new ArrayList<>();
	private List<APOffer> offers;
	
	public StandFaction(Faction base) {
		this.faction = base;
		this.offers = new ArrayList<APOffer>();
		loadOffers(true);
	}
	
	public List<APOffer> getOffers() {
		if(offers == null)
			return new ArrayList<>();
		return offers;
	}
	
	public void addOffer(APOffer offer) {
		allOffers.add(offer);
		getOffers().add(offer);
	}
	
	public void removeOffer(APOffer offer) {
		offer.toDelete();
		Requests.saveAPOffer(offer);
		allOffers.remove(offer);
		getOffers().remove(offer);
		offers = new ArrayList<>();
	}
	
	public APOffer getOffer(FactionChunk fc) {
		for(APOffer offer : offers) {
			if(offer.isChunk(fc))
				return offer;
		}
		return null;
	}
	
	public void loadOffers(boolean syncLoad) {
		offers = null;
		
		Requests.getAPOffers(faction, new Callback<List<APOffer>>() {
			@Override
			public void call(Throwable t, List<APOffer> result) {
				if(t != null || result == null || result.size() == 0) {
					offers = new ArrayList<>();
				} else {
					offers = result;
				}
			}
		});
		
		if(syncLoad) {
			while(offers == null) {
				try {
					Thread.sleep(3L);
				} catch (InterruptedException unused){}
			}
		}
		
		offers.forEach(offer -> allOffers.add(offer));
	}
	
	public void save() {
		Requests.saveAPOffers(this);
	}

	public void openGui(StandPlayer player) {
		APGui gui = new APGui(player, this);
		AbstractInventory before = InventoryManager.getGui(player.getPlayer().getOpenInventory().getTopInventory(), player.getPlayer());
		if(before != null) {
			before.displayGui(gui);
		} else {
			gui.show();
		}
		
	}
	
}
