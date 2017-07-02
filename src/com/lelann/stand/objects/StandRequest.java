package com.lelann.stand.objects;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;import com.avaje.ebeaninternal.server.persist.dml.GenerateDmlRequest;
import com.lelann.factions.utils.ChatUtils;
import com.lelann.stand.Requests;
import com.lelann.stand.abstracts.StandObject;
import com.lelann.stand.selection.MathsUtils;

import fr.devhill.socketinventory.json.bukkit.JSON;
import fr.devhill.socketinventory.json.elements.JObject;
import lombok.Getter;
import lombok.Setter;

public class StandRequest extends StandObject {

	@Getter@Setter private int wantedPrice;
	@Getter@Setter private int wantedAmount;
	@Getter@Setter private int gived;
	@Getter@Setter private int initialAmount;
	@Getter@Setter private Material type;
	@Getter@Setter private byte data;
	@Getter@Setter JObject serializable;
	@Getter@Setter UUID owner;
	public double completedPercent = 0;

	private boolean toCreate = false;
	
	public StandRequest(UUID uniqueId, ItemStack item, int wantedprice, int wantedamount) {
		this.owner = uniqueId;
		this.wantedPrice = wantedprice;
		this.wantedAmount = wantedamount;
		this.initialAmount = wantedamount;
		this.type = item.getType();
		this.data = item.getData().getData();
		this.serializable = JSON.loadFromObject(item);
		this.gived = 0;
		this.toCreate = true;
		
		update();
	}

	public StandRequest(ResultSet set){
		try {
			this.type = Material.matchMaterial(set.getString("type"));
			this.data = (byte) set.getInt("data");
			this.serializable = JSON.loadFromString(set.getString("itemstack"));
			this.wantedAmount = set.getInt("wantedamount");
			this.wantedPrice = set.getInt("wantedprice");
			this.gived = set.getInt("gived");
			this.initialAmount = set.getInt("initialamount");
			this.owner = UUID.fromString(set.getString("owner"));
			this.toCreate = false;
			update();
		} catch(Exception e){}
	}

	public ItemStack createItemStack(String displayName, String... lore){
		ItemStack item = JSON.saveAsObject(serializable, ItemStack.class);
		item.setAmount(wantedAmount);
		ItemMeta meta = item.getItemMeta();

		if(meta != null){
			meta.setDisplayName(ChatUtils.colorReplace(displayName));
			meta.setLore(Arrays.asList(ChatUtils.colorReplace(lore)));
		}

		item.setItemMeta(meta);
		return item;
	}

	public ItemStack createItemStack(int amount){
		ItemStack item = JSON.saveAsObject(serializable, ItemStack.class);
		item.setAmount(amount);

		return item;
	}

	public void add(int amount){
		this.wantedAmount += amount;
	}

	public void remove(int amount){
		this.wantedAmount -= amount;
		this.gived += amount;
		update();
	}

	public String getSQLString(){
		if(wantedAmount == 0){
			toCreate = true;
			return "DELETE FROM sRequests WHERE owner='" + owner + "' AND type='" + type.name() + "' AND data=" + data;
		} else if(toCreate){
			toCreate = false;
			return "INSERT INTO sRequests(type, data, itemstack, owner, initialamount, wantedamount, gived, wantedprice) VALUES('" + type.name() + "'"
					+ ", " + data + ", '" + serializable + "', '" + owner + "', " + initialAmount + ", " + wantedAmount + ", " + gived + ", " + wantedPrice + ")";
		} else {
			return "UPDATE sRequests SET initialamount=" + initialAmount + ", wantedamount=" + wantedAmount + ", gived=" + gived + ", wantedprice=" + wantedPrice + " WHERE owner='" + owner + "' AND type='" + type.name() + "' AND data=" + data;
		}
	}

	public ItemStack getItem() {
		return new ItemStack(type, 1, data);
	}

	public String getName() {
		String name = "";
		for(String word : getType().name().split("_")) {
			name+= word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase() + " ";
		}
		name = name.substring(0, name.length()-1);
		return name;
	}
	
	public void update() {
		this.completedPercent = MathsUtils.round(((double) this.gived / (double) this.initialAmount) * 100.0, 0);
	}
	
}
