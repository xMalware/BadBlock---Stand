package com.lelann.stand.objects;

import java.sql.ResultSet;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.lelann.factions.Main;
import com.lelann.factions.api.Faction;
import com.lelann.factions.utils.ItemUtils;
import com.lelann.stand.abstracts.StandObject;
import com.lelann.stand.selection.MathsUtils;

import lombok.Getter;
import lombok.Setter;

public class APRequest extends StandObject {

	@Getter@Setter private int wantedPrice;
	@Getter@Setter Faction owner;
	@Getter@Setter private int wantedAmount, gived, initialAmount;

	private boolean toCreate = false;
	
	public APRequest(Faction owner, int wantedPrice, int wantedAmount) {
		this.owner = owner;
		this.wantedPrice = wantedPrice;
		this.wantedAmount = wantedAmount;
		this.initialAmount = wantedAmount;
		this.gived = 0;
		this.toCreate = true;
	}

	public APRequest(ResultSet set){
		try {
			this.wantedAmount = set.getInt("wantedamount");
			this.wantedPrice = set.getInt("wantedprice");
			this.gived = set.getInt("gived");
			this.initialAmount = set.getInt("initialamount");
			this.owner = Main.getInstance().getFactionsManager().getFaction(set.getInt("owner"));
			this.toCreate = false;
		} catch(Exception e){}
	}

	public void add(int amount){
		this.wantedAmount += amount;
	}

	public void remove(int amount){
		this.wantedAmount -= amount;
		this.gived += amount;
	}

	public String getSQLString(){
		if(wantedAmount <= 0) {
			toCreate = true;
			return "DELETE FROM sAPRequests WHERE owner=" + owner.getFactionId();
		} else if(toCreate){
			toCreate = false;
			return "INSERT INTO sAPRequests(owner, initialamount, wantedamount, gived, wantedprice) VALUES(" + owner.getFactionId() + ", " + initialAmount + ", " + wantedAmount + ", " + gived + ", " + wantedPrice + ")";
		} else {
			return "UPDATE sAPRequests SET wantedprice=" + wantedPrice + ", wantedamount=" + wantedAmount + ", gived=" + gived + " WHERE owner=" + owner.getFactionId();
		}
	}
	
	public ItemStack createItemStack() {
		ItemStack base = ItemUtils.create(getName(), new String[] {
				"&7Prix voulu: &6" + getWantedPrice() + "$"}, Material.OBSIDIAN);
		return base;
	}

	public String getName() {
		return "&7Demande d'AP de &6" + getOwner().getName();
	}
	
	public double getCompletedPercent() {
		return MathsUtils.round((double) ((double) gived / (double) initialAmount) * 100.0, 0);
	}
	
}
