package com.lelann.stand.objects;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.EulerAngle;

import com.lelann.factions.Main;
import com.lelann.factions.api.FactionPlayer;
import com.lelann.factions.database.Callback;
import com.lelann.factions.utils.ChatUtils;
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
	
	@Getter@Setter private StandAction action;
	@Getter@Setter private Object actionCache;
	
	@Getter@Setter private long standRemove;
	@Getter@Setter private Location standLoc;
	
	private boolean toCreate = false;
	
	public List<StandOffer> getOffers(){
		if(offers == null)
			offers = new ArrayList<>();
		return offers;
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
		
		//loadOffers(syncLoad);
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
	
	private void loadOffers(boolean syncLoad){
		offers = null;
		
		Requests.getOffers(uniqueId, new Callback<List<StandOffer>>(){
			@Override
			public void call(Throwable t, List<StandOffer> result) {
				if(t == null){
					offers = result;
				} else {
					offers = new ArrayList<StandOffer>();
				}
			}
		});
		
		if(syncLoad){
			while(offers == null){
				try {
					Thread.sleep(3L);
				} catch (InterruptedException unused){}
			}
		}
		
		System.out.println("loaded");
	}
	
	public boolean hasEnough(int money){
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
	
	public void add(int money){
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
		AbstractInventory before = InventoryManager.getGui(to.getOpenInventory().getTopInventory());
		if(before != null) {
			before.displayGui(gui);
		} else {
			gui.show(to);
		}
	}
	
	@Deprecated
	public void openeStand(Player to){
		int size = getOffers().size();
		size += size % 9 == 0 ? 0 : 9 - (size % 9);
		
		if(size == 0){
			ChatUtils.sendMessage(to, "&cCe joueur n'a aucune offre à vendre."); return;
		}
		
		/*if(size > 45) {
			//Plus de 6 lignes
			ChatUtils.sendMessage(to, "&cLe stand de ce joueur n'est pas disponible"); return;
		}*/
		
		
		
		boolean canModify = uniqueId.equals(to.getUniqueId()) || to.hasPermission("stand.admin.modify");
		
		Inventory inv = Bukkit.createInventory(null, size, ChatUtils.colorReplace(getStandName()));
		for(int i=0;i<getOffers().size();i++){
			StandOffer offer = getOffers().get(i);
			inv.setItem(i, offer.createItemStack("&cVente à &a" + offer.getPrice() + "$ &cl'unité"
					, "&4> &aClique gauche pour acheter"
					, canModify ? "&4> &cClique droit pour supprimer l'offre" : ""));
		}
		
		StandPlayer player = getPlayer(to);
		
		to.openInventory(inv);
		
		player.setAction(StandAction.LOOKING_STAND);
		player.setActionCache(this);
	}
	
	public void buyOffer(StandPlayer to, StandOffer offer){
		int half = offer.getAmount() / 2;
		int amount = offer.getAmount();
		
		List<Integer> quant = add(half, amount, 1, 8, 16, 32, 64);
		Inventory inv = Bukkit.createInventory(null, 9, ChatUtils.colorReplace("&cChoisissez la quantité :"));
		
		for(int i=0;i<quant.size();i++){
			int quantity = quant.get(i);
			ItemStack item = offer.createItemStack("&cAcheter x" + quantity,
					"&4> &cAcheter x" + quantity + " cet item pour &a" + (quantity * offer.getPrice()) + "$");
			item.setAmount(quantity);
			inv.setItem(i, item);
		}
		
		ItemStack item = new ItemStack(Material.DARK_OAK_DOOR_ITEM, 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatUtils.colorReplace("&aCliquez pour revenir au Stand '" + standName + "'"));
		item.setItemMeta(meta);
		
		inv.setItem(8, item);
		
		to.getPlayer().openInventory(inv);
		
		to.setAction(StandAction.BUYING_OFFER);
		to.setActionCache(offer);
	}
	
	public static List<Integer> add(int half, int amount, int... quant){
		List<Integer> result = new ArrayList<Integer>();
		half = half == 0 ? -1 : half;
		for(int i=0;i<quant.length;i++){
			int nbr = quant[i];
			if(nbr >= half && half != -1){
				result.add(half); half = -1; continue;
			}
			if(nbr >= amount){
				result.add(amount); amount = -1; break;
			}
			
			result.add(nbr);
		}
		
		if(half != -1) result.add(half);
		if(amount != -1) result.add(amount);
		
		return result;
	}
	
	public void removeOffer(StandOffer offer){
		offer.setAmount(0);
		Requests.saveOffer(offer);
		getOffers().remove(offer);
	}
	
	public int getMaxOfferNumber(){
		if(isValid())
			return StandConfiguration.getInstance().allowedOfferNumber(getPlayer());
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
		
		if(toCreate){
			toCreate = false;
			result = "INSERT INTO sPlayers(uniqueId, standName, standRemove, loc) VALUES('" + uniqueId + "'"
					+ ", '" + standName.replace("'", "\\'") + "', " + standRemove + ", " + loc + ")";
		} else {
			result = "UPDATE sPlayers SET standName='" + standName.replace("'", "\\'") + "', standRemove=" + standRemove + ", loc=" + loc + " WHERE uniqueId='" + uniqueId + "'";
		}
		
		return result;
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
