package com.lelann.stand;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

import com.lelann.factions.Main;
import com.lelann.factions.api.Faction;
import com.lelann.factions.api.FactionPlayer;
import com.lelann.factions.database.Callback;
import com.lelann.factions.database.Database;
import com.lelann.stand.objects.APOffer;
import com.lelann.stand.objects.APRequest;
import com.lelann.stand.objects.StandFaction;
import com.lelann.stand.objects.StandOffer;
import com.lelann.stand.objects.StandPlayer;
import com.lelann.stand.objects.StandRequest;

public class Requests {
	public static Database getDB(){
		return Main.getInstance().getDB();
	}
	
	public static void saveOffer(final StandOffer offer){
		getDB().updateAsynchrounously(offer.getSQLString());
	}
	
	public static void saveAPOffer(final APOffer offer) {
		getDB().updateAsynchrounously(offer.getSQLString());
	}
	
	public static void savePlayer(final StandPlayer player){
		getDB().updateAsynchrounously(player.getSQLString());
		
		for(StandOffer offer : player.getOffers()){
			saveOffer(offer);
		}
		
		for(StandRequest request : player.getRequests()) {
			saveRequest(request);
		}
	}
	
	public static void getOffers(final UUID uniqueId, final Callback<List<StandOffer>> done){
		new Thread(){
			@Override
			public void run(){
				Throwable t = null;
				List<StandOffer> result = new ArrayList<StandOffer>();
				
				try {
					String request = "SELECT * FROM sOffers WHERE owner='" + uniqueId + "'";
					
					ResultSet set = getDB().querySQL(request);
					while(set.next()){
						StandOffer offer = new StandOffer(set);
						result.add(offer);
					}
				} catch (Throwable throwable) {
					t = throwable;
				}
				
				done.call(t, result);
			}
		}.start();
	}
	
	public static void getTop(ItemStack item, int limit, boolean cheap, Callback<List<StandOffer>> done) {
		List<StandOffer> offers = offers(item);
		if(offers == null) {
			System.out.println("could not find any offer for item " + item.getType());
			done.call(new Throwable("Pas d'offre pour cet item."), null);
		}
		List<StandOffer> base = offers(item);
		base.sort(Comparator.comparing(StandOffer::getPrice));
		if(!cheap) {
			Collections.reverse(base);
		}
		base = base.subList(0, limit >= offers.size() ? offers.size() : limit);
		done.call(null, base);
	}
	
	@SuppressWarnings("deprecation")
	public static List<StandOffer> offers(ItemStack item) {
		List<StandOffer> base = new ArrayList<>();
		for(StandOffer offer : StandPlayer.allOffers) {
			if(offer.getType() == item.getType() && offer.getData() == item.getData().getData()) {
				FactionPlayer owner = Main.getInstance().getPlayersManager().getPlayer(offer.getOwner());
				if(owner == null) owner = Main.getInstance().getPlayersManager().addPlayer(offer.getOwner());
				if(offer.getAmount() <= 0) continue;
				base.add(offer);
			}
		}
		return base;
	}
	
	@Deprecated
	public static void geteTop(final ItemStack item, final int limit, final boolean cheap, final Callback<List<StandOffer>> done){
		
		new Thread(){
			@Override
			public void run(){
				Throwable t = null;
				List<StandOffer> result = new ArrayList<StandOffer>();
				
				try {
					
					String action = "ASC";
					if(!cheap) action = "DESC";
					
					String type = item.getType().name();
					int data = item.getData().getData();
					
					String request = "SELECT * FROM sOffers WHERE type='" + type 
							+ "' AND data=" + data + " ORDER BY price " + action + " LIMIT 0,"
							+ limit;

					ResultSet set = getDB().querySQL(request);
					while(set.next()){
						StandPlayer player = StandPlugin.get().getPlayer(UUID.fromString(set.getString("owner")));
//						if(Bukkit.getPlayer(player.getUniqueId()) == null) continue;
						
						Collection<StandOffer> offers = Collections.unmodifiableCollection(player.getOffers());
						
						for(StandOffer offer : offers){
							if(offer.getType() == item.getType() && offer.getData() == data){
								result.add(offer); break;
							}
						}
					}
				} catch (Throwable throwable) {
					System.out.println("==== Stand TOP Debuging ====");
					throwable.printStackTrace();
					t = throwable;
				}
				
				done.call(t, result);
			}
		}.start();
	}

	public static void getRequests(UUID uniqueId, Callback<List<StandRequest>> done) {
		new Thread(){
			@Override
			public void run(){
				Throwable t = null;
				List<StandRequest> result = new ArrayList<StandRequest>();
				
				try {
					String query = "SELECT * FROM sRequests WHERE owner='" + uniqueId + "'";
					
					ResultSet set = getDB().querySQL(query);
					while(set.next()){
						StandRequest request = new StandRequest(set);
						result.add(request);
					}
				} catch (Throwable throwable) {
					t = throwable;
				}
				
				done.call(t, result);
			}
		}.start();
	}

	public static void saveRequest(StandRequest request) {
		getDB().updateAsynchrounously(request.getSQLString());
	}
	
	@SuppressWarnings("deprecation")
	public static List<StandRequest> requests(ItemStack item) {
		List<StandRequest> base = new ArrayList<>();
		for(StandRequest request : StandPlayer.allRequests) {
			if(request.getType() == item.getType() && request.getData() == item.getData().getData()) {
				FactionPlayer owner = Main.getInstance().getPlayersManager().getPlayer(request.getOwner());
				StandPlayer player = request.getPlayer(request.getOwner());
				if(player.getCompleted().contains(request)) continue;
				if(owner == null) continue;
				if(request.getWantedAmount() <= 0) continue;
				base.add(request);
			}
		}
		return base;
	}
	
	public static void getTopRequests(ItemStack item, int limit, Callback<List<StandRequest>> done) {
		List<StandRequest> requests = requests(item);
		if(requests == null) done.call(new Throwable("Pas de demande pour cet item."), null);
		List<StandRequest> base = requests(item);
		base.sort(Comparator.comparing(StandRequest::getWantedPrice));
		Collections.reverse(base);
		base = base.subList(0, limit >= requests.size() ? requests.size() : limit);
		done.call(null, base);
	}
	
	public static void getAPOffers(final Faction owner, final Callback<List<APOffer>> done){
		new Thread(){
			@Override
			public void run(){
				Throwable t = null;
				List<APOffer> result = new ArrayList<APOffer>();
				
				try {
					String request = "SELECT * FROM sAPOffers WHERE owner='" + owner.getFactionId() + "'";
					
					ResultSet set = getDB().querySQL(request);
					while(set.next()){
						APOffer offer = new APOffer(set);
						result.add(offer);
					}
				} catch (Throwable throwable) {
					t = throwable;
				}
				
				done.call(t, result);
			}
		}.start();
	}
	
	public static List<APOffer> aps() {
		List<APOffer> base = new ArrayList<>();
		for(APOffer apOffer : StandFaction.allOffers) {
			base.add(apOffer);
		}
		return base;
	}
	
	public static void getAPsOnSale(int limit, Callback<List<APOffer>> done) {
		List<APOffer> aps = aps();
		if(aps == null) done.call(new Throwable("Pas de vente d'APs."), null);
		List<APOffer> base = aps();
		base.sort(Comparator.comparing(APOffer::getPrice));
		base = base.subList(0, limit >= aps.size() ? aps.size() : limit);
		done.call(null, base);
		
	}

	public static void saveAPOffers(StandFaction standFaction) {
		for(APOffer offer : standFaction.getOffers()) {
			if(offer == null) {
				System.out.println("offer is null WTFFFF ??,!!,!,!,");
				continue;
			}
			
			//System.out.println("saving offer " + offer.getAp().toString() + ", owner: " + offer.getOwner().getFactionId());
			saveAPOffer(offer);
		}
	}

	public static void saveAPRequest(APRequest req) {
		getDB().updateAsynchrounously(req.getSQLString());
	}

	public static void getAPRequests(Faction faction, Callback<List<APRequest>> done) {
		new Thread() {
			@Override
			public void run() {
				Throwable t = null;
				List<APRequest> result = new ArrayList<>();
				
				try {
					String query = "SELECT * FROM sAPRequests WHERE owner=" + faction.getFactionId();
					
					ResultSet set = getDB().querySQL(query);
					while(set.next()) {
						result.add(new APRequest(set));
					}
				} catch (Throwable throwable) {
					t = throwable;
				}
				
				done.call(t, result);
			}
		}.start();
		
	}

	public static void saveAPRequests(StandFaction faction) {
		for(APRequest req : faction.getRequests()) {
			saveAPRequest(req);
		}
	}

	public static List<APRequest> aprequests() {
		List<APRequest> base = new ArrayList<>();
		for(APRequest apReq : StandFaction.allRequests) {
			base.add(apReq);
		}
		return base;
	}
	
	public static void getAPRequests(int limit, Callback<List<APRequest>> done) {
		List<APRequest> aps = aprequests();
		if(aps == null) done.call(new Throwable("Pas de demandes d'APs."), null);
		List<APRequest> base = aprequests();
		base.sort(Comparator.comparing(APRequest::getWantedPrice));
		Collections.reverse(base);
		base = base.subList(0, limit >= aps.size() ? aps.size() : limit);
		done.call(null, base);
		
	}
	
}
