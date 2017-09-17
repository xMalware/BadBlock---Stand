package com.lelann.stand.events;

import org.bukkit.event.HandlerList;

import com.lelann.stand.objects.StandOffer;
import com.lelann.stand.objects.StandPlayer;

import lombok.Getter;

public class ItemBoughtEvent extends StandPlayerEvent {
	
	private static final HandlerList handlers = new HandlerList();
	
	@Getter
	private StandOffer offer;
	@Getter
	private int amount;
	
	public ItemBoughtEvent(StandPlayer who, StandOffer offer, int amount) {
		super(who);
		this.offer = offer;
		this.amount = amount;
		
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
