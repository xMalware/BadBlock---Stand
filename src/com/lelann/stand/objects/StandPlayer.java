package com.lelann.stand.objects;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.EulerAngle;

import com.lelann.factions.Main;
import com.lelann.factions.api.FactionPlayer;
import com.lelann.factions.database.Callback;
import com.lelann.factions.utils.ChatUtils;
import com.lelann.factions.utils.JRawMessage;
import com.lelann.factions.utils.JRawMessage.ClickEventType;
import com.lelann.factions.utils.JRawMessage.HoverEventType;
import com.lelann.stand.Requests;
import com.lelann.stand.StandConfiguration;
import com.lelann.stand.StandPlugin;
import com.lelann.stand.abstracts.StandObject;
import com.lelann.stand.inventories.StandGUI;
import com.lelann.stand.inventories.abstracts.AbstractInventory;
import com.lelann.stand.inventories.abstracts.InventoryManager;

import fr.devhill.socketinventory.json.bukkit.JSON;
import lombok.Getter;
import lombok.Setter;

public class StandPlayer extends StandObject {
	@Getter private UUID uniqueId;
	@Getter private String playerName;
	
	@Getter@Setter private String standName;
	private List<StandOffer> offers;
	private List<StandRequest> requests;
	
	@Getter@Setter private StandAction action;
	@Getter@Setter private Object actionCache;
	
	@Getter@Setter private long standRemove;
	@Getter@Setter private Location standLoc;
	
	public static List<StandOffer> allOffers = new ArrayList<>();
	public static List<StandRequest> allRequests = new ArrayList<>();
	
	@Getter private List<StandRequest> completed = new ArrayList<>();
	@Getter private Map<StandRequest, Integer> waiting = new HashMap<>();
	
	private boolean toCreate = false;
	
	public List<StandOffer> getOffers(){
		if(offers == null)
			offers = new ArrayList<>();
		return offers;
	}
	
	public List<StandRequest> getRequests(){
		if(requests == null)
			requests = new ArrayList<>();
		return requests;
	}
	
	public StandPlayer(Player player){
		this.uniqueId = player.getUniqueId();
		this.playerName = player.getName();
		this.standName = ChatUtils.colorReplace("&c" + playerName + "'s stand");
		this.offers = new ArrayList<StandOffer>();
		this.toCreate = true;
		this.standRemove = 0;
	}
	
	public StandPlayer(ResultSet set, boolean syncLoad) throws Exception {
		uniqueId = UUID.fromString(set.getString("uniqueId"));
		standName = set.getString("standName");
		standRemove = set.getLong("standRemove");
		
		String temp = set.getString("loc");
		if(temp != null){
			standLoc = JSON.saveAsObject(JSON.loadFromString(temp), Location.class);
		}
		
		loadOffers(syncLoad);
		loadRequests(syncLoad);
		
		//LOADING ITEMS THAT HAS NOT BEEN REDEEM
		//[{DIAMOND_BOOTS:0}=24,{STONE:5=12}]
		String items = set.getString("requests");
		if(items != null && !items.isEmpty() && items.length() > 0) {
			items = items.replaceAll("\\{", "").replaceAll("\\}", "").replaceAll("\\[", "").replaceAll("\\]", "");
			if(items.contains(","))
				for(String parts : items.split(",")) {
					String item = parts.split("=")[0];
					String id = item.split(":")[0];
					String data = item.split(":")[1];
					String a = parts.split("=")[1];
					
					Material mat = Material.valueOf(id);
					byte d = Byte.parseByte(data);
					int amount = Integer.parseInt(a);
					
					ItemStack s = new ItemStack(mat, amount, d);
					StandRequest request = getRequest(s);
					
					waiting.put(request, amount);
				}
		}
		toCreate = false;
	}
	
	@SuppressWarnings("deprecation")
	public StandOffer getOffer(ItemStack item){
		Material type = item.getType();
		byte data = item.getData().getData();
		
		for(StandOffer offer : offers){
			if(offer.getData() == data && offer.getType() == type)
				return offer;
		}
		
		return null;
	}
	
	@SuppressWarnings("deprecation")
	public StandRequest getRequest(ItemStack item){
		Material type = item.getType();
		byte data = item.getData().getData();
		
		for(StandRequest request : requests){
			if(request.getData() == data && request.getType() == type)
				return request;
		}
		
		return null;
	}
	
	public void addRequest(StandRequest request) {
		getRequests().add(request);
		allRequests.add(request);
		Requests.saveRequest(request);
	}
	
	public void removeRequest(StandRequest request) {
//		if(!completed.contains(request)) completed.add(request);
		request.setWantedAmount(0);
		//Requests.saveRequest(request);
		//getRequests().remove(request);
		//allRequests.remove(request);
		updateRequest(request);
	}
	
	public void deleteRequest(StandRequest request) {
		request.setWantedAmount(0);
		getRequests().remove(request);
		allRequests.remove(request);
		waiting.remove(request);
		Requests.saveRequest(request);
	}
	
	public void updateRequest(StandRequest request) {
		if(request.getWantedAmount() <= 0)
			completed.add(request);
		else request.update();
	}
	
	public void listRequests() {
		sendMessage(header("Demandes"));
		
		for(StandRequest request : requests) {
			String color = "&6";
			if(request.getGived() == 0) color = "&c";
			if(completed.contains(request)) color = "&a";
			JRawMessage msg = new JRawMessage(color + "* &7" + request.getName() + " &8(" + color + request.completedPercent + "%&8)&f - ");	 
			JRawMessage give = null;
			if(waiting.get(request) != null && waiting.get(request) > 0) {
				give = new JRawMessage("&3GET");
				give.addClickEvent(ClickEventType.RUN_COMMAND, "/stand buy getitems " + request.getType() + ":" + request.getData(), false);
				give.addHoverEvent(HoverEventType.SHOW_TEXT, "§7Obtenir les items qui vous ont été vendus", false);
			}
			JRawMessage delete = null;
			if(!completed.contains(request)) {
				delete = new JRawMessage("&cDEL");
				delete.addClickEvent(ClickEventType.RUN_COMMAND, "/stand buy removerequest " + request.getType() + ":" + request.getData(), false);
				delete.addHoverEvent(HoverEventType.SHOW_TEXT, "§7Supprime votre demande", false);
			}
			
			if(give != null)
				msg.add(give);
			
			if(delete != null)
				msg.add(delete);
			
			msg.send(getPlayer());
		}
		
		sendMessage(footer("Demandes"));
	}
	
	public void addOffer(StandOffer offer) {
		getOffers().add(offer);
		allOffers.add(offer);
	}
	
	public void removeOffer(StandOffer offer) {
		offer.setAmount(0);
		Requests.saveOffer(offer);
		getOffers().remove(offer);
		allOffers.remove(offer);
	}
	
	private void loadOffers(boolean syncLoad) {
		offers = null;
		
		Requests.getOffers(uniqueId, new Callback<List<StandOffer>>() {
			@Override
			public void call(Throwable t, List<StandOffer> result) {
				if(t == null){
					offers = result;
					//System.out.println("Loaded " + result.size() + " offers for player " + uniqueId);
				} else {
					offers = new ArrayList<StandOffer>();
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
	
	private void loadRequests(boolean syncLoad) {
		requests = null;
		
		Requests.getRequests(uniqueId, new Callback<List<StandRequest>>() {
			@Override
			public void call(Throwable t, List<StandRequest> result) {
				if(t == null){
					requests = result;
					//System.out.println("Loaded " + result.size() + " offers for player " + uniqueId);
				} else {
					requests = new ArrayList<StandRequest>();
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
		
		requests.forEach(request -> allRequests.add(request));
	}
	
	public boolean hasEnough(int money) {
		FactionPlayer player = Main.getInstance().getPlayersManager().getPlayer(uniqueId);
		
		return player == null ? false : player.hasEnough(money);
	}
	
	public long getMoney() {
		FactionPlayer player = Main.getInstance().getPlayersManager().getPlayer(uniqueId);
		
		return player == null ? 0 : player.getMoney();
	}
	
	public void remove(long money){
		FactionPlayer player = Main.getInstance().getPlayersManager().getPlayer(uniqueId);
		if(player != null) {
			player.removeMoney(money);
			player.save(false);
		}
	}
	
	public void add(long money){
		FactionPlayer player = Main.getInstance().getPlayersManager().getPlayer(uniqueId);
		if(player != null) {
			player.addMoney(money);
			player.save(false);
		}
	}
	
	public void sendMessage(String message){
		if(isValid())
			ChatUtils.sendMessage(getPlayer(), message);
	}
	
	public void sendMessages(String... messages){
		for(String message : messages)
			sendMessage(message);
	}
	
	public static int random(int min, int max){
		return (int)(Math.random() * (max + 1 - min)) + min;		
	}
	
	public ArmorStand place(String name, ItemStack item, Location loc) {
		ArmorStand stand = loc.getWorld().spawn(loc, ArmorStand.class);
		stand.setBasePlate(false);
		stand.setArms(true);
		stand.setCanPickupItems(false);
		stand.setCustomNameVisible(true);
		stand.setCustomName(standName);
		
		int random = random(1, 3);
		
		if(random == 1) {
			stand.setHeadPose(new EulerAngle(Math.toRadians(40), 0, 0));
			
			stand.setLeftLegPose(new EulerAngle(Math.toRadians(32), 0, 0));
			stand.setLeftArmPose(new EulerAngle(Math.toRadians(312), 0, 0));
			
			stand.setRightLegPose(new EulerAngle(Math.toRadians(323), 0, 0));
			stand.setRightArmPose(new EulerAngle(Math.toRadians(43), 0, 0));
		} else if(random == 2) {
			stand.setHeadPose(new EulerAngle(0, Math.toRadians(31), 0));
			
			stand.setLeftLegPose(new EulerAngle(Math.toRadians(23), 0, 0));
			stand.setLeftArmPose(new EulerAngle(Math.toRadians(295), 0, 0));
			
			stand.setRightLegPose(new EulerAngle(Math.toRadians(309), 0, 0));
			stand.setRightArmPose(new EulerAngle(Math.toRadians(321), 0, 0));
		}
		
		ItemStack helmet = new ItemStack(Material.SKULL_ITEM);
		helmet.setDurability((short) 3);
		
		SkullMeta meta = (SkullMeta) helmet.getItemMeta();
		meta.setOwner(name); helmet.setItemMeta(meta);
		
		stand.setHelmet(helmet);
		stand.setItemInHand((item == null || item.getType() == Material.AIR) ? new ItemStack(Material.WORKBENCH, 1) : item.clone());
		stand.setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
		stand.setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
		stand.setBoots(new ItemStack(Material.DIAMOND_BOOTS));
		
		stand.setRemoveWhenFarAway(false);
		
		StandPlugin.get().setArmorStand(getUniqueId(), stand);
		
		this.standRemove = System.currentTimeMillis() + 24 * 3_600_000;
		this.standLoc = stand.getLocation();
		
		return stand;
	}
	
	public void openStand(Player to) {
		StandGUI gui = new StandGUI(to, this);
		AbstractInventory before = InventoryManager.getGui(to.getOpenInventory().getTopInventory(), to);
		if(before != null) {
			before.displayGui(gui);
		} else {
			gui.show();
		}
	}
	
	public int getMaxOfferNumber(){
		if(isValid())
			return StandConfiguration.getInstance().allowedOfferNumber(getPlayer());
		else return 0;
	}
	
	public int getMaxRequestNumber() {
		if(isValid())
			return StandConfiguration.getInstance().allowedRequestNumber(getPlayer());
		else return 0;
	}
	
	public Player getPlayer(){
		return getServer().getPlayer(uniqueId);
	}
	
	public boolean isValid(){
		Player p = getPlayer();
		return p != null && p.isOnline() && p.isValid();
	}
	
	public String getSQLString(){
		String loc = standLoc == null ? "NULL" : "'" + JSON.loadFromObject(standLoc).toString().replace("'", "\\'") + "'";
		
		String result = "";
		
		if(toCreate) {
			toCreate = false;
			result = "INSERT INTO sPlayers(uniqueId, standName, standRemove, loc, requests) VALUES('" + uniqueId + "'"
					+ ", '" + standName.replace("'", "\\'") + "', " + standRemove + ", " + loc + ", '" + requestsToString() + "')";
		} else {
			result = "UPDATE sPlayers SET standName='" + standName.replace("'", "\\'") + "', standRemove=" + standRemove + ", loc=" + loc + ", requests='" + requestsToString() + "' WHERE uniqueId='" + uniqueId + "'";
		}
		
		return result;
	}
	
	private String requestsToString() {
		//[{DIAMOND_BOOTS:0}=24,{STONE:5=12}]
		if(waiting.keySet().size() == 0) return "";
		String base = "[";
		for(StandRequest request : waiting.keySet()) {
			if(waiting.get(request) == null) continue;
			if(request == null) continue;
			base+= "{" + request.getType() + ":" + request.getData() + "}=" + waiting.get(request) + ",";
		}
		base = base.substring(0, base.length()-1);
		base+="]";
		if(base.length() <= 1 || base.equals("]")) return "";
		return base;
	}
	
	private List<JRawMessage> offline = new ArrayList<>();
	
	public void addRequestMsg(JRawMessage messsage) {
		offline.add(messsage);
	}
	
	public void sendRequestMessages() {
		offline.forEach(msg -> msg.send(getPlayer()));
	}

	public boolean hasRequestedSame(StandRequest r) {
		for(StandRequest request : requests) {
			if(request.getType() == r.getType() && request.getData() == r.getData()) return true;
		}
		return false;
	}
	
	public enum StandAction {
		LOOKING_PNJ,
		LOOKING_TOP,
		LOOKING_STAND,
		BUYING_OFFER,
		WAITING,
		A_WAITING_DEL_PNJ,
		A_WAITING_CHANGE_NAME,
		A_LOOKING_PNJ,
		WAITING_KICK,
		NOTHING;
	}

}
