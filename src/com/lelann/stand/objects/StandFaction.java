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
import sun.security.krb5.internal.APReq;

public class StandFaction extends StandObject {

	@Getter private Faction faction;
	
	public static List<APOffer> allOffers = new ArrayList<>();
	private List<APOffer> offers;
	
	public static List<APRequest> allRequests = new ArrayList<>();
	@Getter private List<APRequest> requests;
	
	public StandFaction(Faction base) {
		this.faction = base;
		this.offers = new ArrayList<APOffer>();
		this.requests = new ArrayList<>();
		loadOffers(true);
		loadRequests(true);
	}
	
	public void addRequest(APRequest request) {
		getRequests().add(request);
		allRequests.add(request);
		Requests.saveAPRequest(request);
	}
	
	public void removeRequest(APRequest request) {
//		if(!completed.contains(request)) completed.add(request);
		request.setWantedAmount(0);
		//Requests.saveRequest(request);
		//getRequests().remove(request);
		//allRequests.remove(request);
		updateRequest(request);
	}
	
	public void deleteRequest(APRequest request) {
		request.setWantedAmount(0);
		getRequests().remove(request);
		Requests.saveAPRequest(request);
	}
	
	public void updateRequest(APRequest request) {
		if(request.getWantedAmount() <= 0)
			deleteRequest(request);
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
	
	public void loadRequests(boolean syncLoad) {
		requests = null;
		
		Requests.getAPRequests(faction, new Callback<List<APRequest>>() {
			@Override
			public void call(Throwable t, List<APRequest> result) {
				if(t != null || result == null) {
					requests = new ArrayList<>();
				} else {
					requests = result;
				}
			}
		});
		
		if(syncLoad) {
			while(requests == null) {
				try {
					Thread.sleep(3L);
				} catch (InterruptedException unused){}
			}
		}
		
		requests.forEach(req -> allRequests.add(req));
	}
	
	
	public void save() {
		Requests.saveAPOffers(this);
		Requests.saveAPRequests(this);
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
