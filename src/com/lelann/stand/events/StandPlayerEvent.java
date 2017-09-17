package com.lelann.stand.events;

import org.bukkit.event.Event;

import com.lelann.stand.objects.StandPlayer;

public abstract class StandPlayerEvent extends Event {

	protected StandPlayer player;

	public StandPlayerEvent(StandPlayer who) {
		this.player = who;
	}

	StandPlayerEvent(StandPlayer who, boolean async) {
		super(async);
		this.player = who;
	}

	public final StandPlayer getPlayer() {
		return this.player;
	}
	
}
