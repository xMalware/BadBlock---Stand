package com.lelann.stand.objects;

import java.sql.ResultSet;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.lelann.factions.Main;
import com.lelann.factions.api.Faction;
import com.lelann.factions.api.FactionChunk;
import com.lelann.factions.utils.ItemUtils;
import com.lelann.stand.abstracts.StandObject;
import com.lelann.stand.selection.MathsUtils;
import com.sk89q.worldguard.util.MathUtils;

import fr.devhill.socketinventory.json.bukkit.JSON;
import fr.devhill.socketinventory.json.elements.JObject;
import lombok.Getter;
import lombok.Setter;

public class APOffer extends StandObject {

	@Getter@Setter private int price;
	@Getter@Setter JObject serializable;
	@Getter@Setter Faction owner;
	@Getter@Setter FactionChunk ap;

	private boolean toCreate = false;
	private boolean remove;
	@Getter private long canBeRemovedAt;
	private int TIME = 24 * 60 * 60 * 1000;

	public APOffer(Faction owner, FactionChunk ap, int price) {
		this.owner = owner;
		this.price = price;
		this.serializable = JSON.loadFromObject(ap);
		this.ap = ap;
		this.toCreate = true;
		this.remove = false;
		this.canBeRemovedAt = System.currentTimeMillis() + TIME;
	}

	public APOffer(ResultSet set){
		try {
			this.serializable = JSON.loadFromString(set.getString("ap"));
			this.price = set.getInt("price");
			this.owner = Main.getInstance().getFactionsManager().getFaction(set.getInt("owner"));
			this.ap = JSON.saveAsObject(serializable, FactionChunk.class);
			this.canBeRemovedAt = set.getLong("canberemovedat");
			this.toCreate = false;
			this.remove = false;
		} catch(Exception e){}
	}

	public String getSQLString() {
		//System.out.println("getSQLString():APOffer->owner=" + owner.getName());
		//System.out.println("saving by sql string: [ADD?]: " + owner.getFactionId());
		if(remove) {
			toCreate = true;
			//System.out.println("[REMOVE]!");
			return "DELETE FROM sAPOffers WHERE owner=" + owner.getFactionId() + " AND ap='" + serializable + "'";
		} else if(toCreate) {
			toCreate = false;
			//System.out.println("[ADD]!");
			return "INSERT INTO sAPOffers(owner, ap, price, canberemovedat) VALUES(" + owner.getFactionId() + ", '" + serializable + "', " + price + ", " + canBeRemovedAt + ")";
		} else {
			//System.out.println("[UPDATE]!");
			return "UPDATE sAPOffers SET price=" + price + " WHERE owner=" + owner.getFactionId() + " AND ap='" + serializable + "'";
		}
	}
	
	public ItemStack createItemStack() {
		ItemStack base = ItemUtils.create(getName(), new String[] {
				"&7Prix: &6" + getPrice() + "$",
				"&7Taxe: &6" + MathsUtils.round(getPrice() * 0.10, 2) + "$",
				"&7Détails de l'AP", "&8-----------", "&7x: &6" + ap.getX(),
				"&7z: &6" + ap.getZ(), "&7Faction: &6" + ap.getOwner().getName()}, Material.OBSIDIAN);
		return base;
	}
	
	public String getFactionName() {
		return owner.getName();
	}

	public String getName() {
		return "&7Mise en vente de l'AP (&6" + ap.toString() + "&7)";
	}
	
	public boolean canRemove() {
		return System.currentTimeMillis() > canBeRemovedAt;
	}

	public boolean isChunk(FactionChunk ap) {
		return ap.getOwner().getFactionId() == owner.getFactionId()
				&& ap.getWorld().equals(this.ap.getWorld())
				&& ap.getX() == this.ap.getX()
				&& ap.getZ() == this.ap.getZ();
	}

	public void toDelete() {
		this.remove = true;
	}
	
}
