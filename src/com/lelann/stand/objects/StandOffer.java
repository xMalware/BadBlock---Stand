package com.lelann.stand.objects;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.lelann.factions.utils.ChatUtils;
import com.lelann.stand.abstracts.StandObject;

import fr.devhill.socketinventory.json.bukkit.JSON;
import fr.devhill.socketinventory.json.elements.JObject;
import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("deprecation")
public class StandOffer extends StandObject {
	@Getter@Setter private int price;
	@Getter@Setter private int amount;
	@Getter@Setter private Material type;
	@Getter@Setter private byte data;
	@Getter@Setter JObject serializable;
	@Getter@Setter UUID owner;

	private boolean toCreate = false;

	public StandOffer(UUID uniqueId, ItemStack item, int price){
		this.owner = uniqueId;
		this.price = price;
		this.amount = item.getAmount();
		this.type = item.getType();
		this.data = item.getData().getData();
		this.serializable = JSON.loadFromObject(item);
		this.toCreate = true;
	}

	public StandOffer(ResultSet set){
		try {
			this.type = Material.matchMaterial(set.getString("type"));
			this.data = (byte) set.getInt("data");
			this.serializable = JSON.loadFromString(set.getString("itemstack"));
			this.amount = set.getInt("amount");
			this.price = set.getInt("price");
			this.owner = UUID.fromString(set.getString("owner"));
			this.toCreate = false;
		} catch(Exception e){}
	}

	public ItemStack createItemStack(String displayName, String... lore){
		ItemStack item = JSON.saveAsObject(serializable, ItemStack.class);
		item.setAmount(amount);
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
		this.amount += amount;
	}

	public void remove(int amount){
		this.amount -= amount;
	}

	public String getSQLString(){
		if(amount == 0){
			toCreate = true;
			return "DELETE FROM sOffers WHERE owner='" + owner + "' AND type='" + type.name() + "' AND data=" + data;
		} else if(toCreate){
			toCreate = false;
			return "INSERT INTO sOffers(type, data, itemstack, owner, amount, price) VALUES('" + type.name() + "'"
					+ ", " + data + ", '" + serializable + "', '" + owner + "', " + amount + ", " + price + ")";
		} else {
			return "UPDATE sOffers SET amount=" + amount + ", price=" + price + " WHERE owner='" + owner + "' AND type='" + type.name() + "' AND data=" + data;
		}
	}

	public ItemStack getItem() {
		return new ItemStack(type, 1, data);
	}
	//	return "DELETE FROM fPlayers WHERE uniqueId='" + uniqueId + "'";
	//	return "UPDATE fPlayers SET "
	//			+ "lastUsername='" + lastUsername + "'"
	//			+ ", factionId=" + factionId
	//			+ ", power=" + power 
	//			+ ", rank='" + factionRank + "'"
	//			+ ", title='" + title + "'"
	//			+ ", lastConnection='" + lastConnection + "'"
	//			+ ", money='" + money + "'"
	//			+ " WHERE uniqueId='" + uniqueId + "'";
}
