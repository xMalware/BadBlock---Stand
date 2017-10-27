package com.lelann.stand.job;

import com.lelann.factions.api.FactionPlayer;
import com.lelann.factions.api.jobs.Job;

public class SellerJob extends Job {

	public SellerJob() {
		super("Vendeur", new String[] {"&7Job quand tu vends masse items"}, 4);
	}

	@Override
	public void onAssign(FactionPlayer p) {
	
		this.setObject(p, "bypass-taxes", false);
		this.setObject(p, "multiplicator", 1.1);
		
		super.onAssign(p);
	}
	
	@Override
	public void onRevoke(FactionPlayer p) {
		this.removeObjects(p);
		super.onRevoke(p);
	}
	
	@Override
	public void onLevelIncrement(FactionPlayer p) {
		
		System.out.println("increment! level: " + p.getJobValues(getClass()).getCurrentLevel() + " MAX: " + this.getMaxLevel());
		
		if(p.getJobValues(getClass()).getCurrentLevel() >= this.getMaxLevel()) {
			this.setObject(p, "multiplicator", (double) this.getObject(p, "multiplicator") + 0.1);
			this.setObject(p, "bypass-taxes", true);
			p.sendMessage(this.prefix + "Bravo ! Vous avez atteind le niveau maximal qui est le niveau &b" + this.getMaxLevel() + " &7!");
			p.sendMessage(this.prefix + "Par conséquent, vous ne subissez plus les taxes lors de vos achats dans les stands !");
		} else if(p.getJobValues(getClass()).getCurrentLevel() < this.getMaxLevel()) {
			this.setObject(p, "multiplicator", (double) this.getObject(p, "multiplicator") + 0.2);
		}
		
		super.onLevelIncrement(p);
	}
	
}
