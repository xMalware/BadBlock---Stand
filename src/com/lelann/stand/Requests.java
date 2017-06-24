package com.lelann.stand;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

import com.lelann.factions.Main;
import com.lelann.factions.database.Callback;
import com.lelann.factions.database.Database;
import com.lelann.stand.objects.StandOffer;
import com.lelann.stand.objects.StandPlayer;

public class Requests {
	public static Database getDB(){
		return Main.getInstance().getDB();
	}
	
	public static void saveOffer(final StandOffer offer){
		getDB().updateAsynchrounously(offer.getSQLString());
	}
	
	public static void savePlayer(final StandPlayer player){
		getDB().updateAsynchrounously(player.getSQLString());
		
		for(StandOffer offer : player.getOffers()){
			saveOffer(offer);
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
	
	public static void getTop(final ItemStack item, final int limit, final boolean cheap, final Callback<List<StandOffer>> done){
		
		new Thread(){
			@SuppressWarnings("deprecation")
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
}
