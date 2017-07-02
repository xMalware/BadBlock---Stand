package com.lelann.stand.listeners;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

import com.lelann.factions.runnables.FRunnable;
import com.lelann.factions.utils.ChatUtils;
import com.lelann.factions.utils.JRawMessage;
import com.lelann.factions.utils.JRawMessage.ClickEventType;
import com.lelann.stand.abstracts.StandObject;
import com.lelann.stand.objects.ApPNJ;
import com.lelann.stand.objects.CategoryPNJ;

import lombok.Getter;

public class CategoryPNJManager extends StandObject {

	@Getter
	private Map<UUID, CategoryPNJ> pnjs = new HashMap<>();
	@Getter private ApPNJ ap = null;
	
	@Getter
	private Map<String, CategoryPNJ> identifiers = new HashMap<>();
	
	public CategoryPNJManager(List<CategoryPNJ> pnjs) {
		for(final CategoryPNJ pnj : pnjs) {
			add(pnj);
		}
	}
	
	public void reload() {
		
	}
	
	public void add(CategoryPNJ pnj) {
		final Villager entity = (Villager) pnj.createEntity();
		pnjs.put(entity.getUniqueId(), pnj);
		identifiers.put(pnj.getIdentifier(), pnj);
		
		for(Entity e : entity.getNearbyEntities(1.0f, 1.0f, 1.0f)){
			if(entity.getCustomName().equals(e.getCustomName()))
				e.remove();
		}
		
		new FRunnable(40L){
			@Override
			public void run(){
				if(entity == null || entity.isDead()) {
					cancel(); return;
				}
				Location loc = pnj.getLocation().clone();
				loc.setYaw(entity.getLocation().getYaw());
				loc.setPitch(entity.getLocation().getPitch());
				
				entity.teleport(loc);
			}
		}.start();
	}
	
	public void savePnjs(){
		for(CategoryPNJ pnj : pnjs.values()){
			pnj.save();
		}
		if(ap != null) ap.save();
	}

	public boolean isPnj(Entity entity) {
		return pnjs.containsKey(entity.getUniqueId()) || (ap != null && ap.getEntity().getUniqueId().equals(entity.getUniqueId()));
	}

	public ApPNJ getApPnj() {
		return ap;
	}
	
	public CategoryPNJ getPnj(Entity e) {
		return pnjs.get(e.getUniqueId());
	}
	
	public CategoryPNJ getPnj(String identifier) {
		return identifiers.get(identifier);
	}

	public void sendList(Player p) {
		String PATTERN = "%name% &8[%id%]&f <>&7 %loc% &f-";
		JRawMessage space = new JRawMessage(" ");
		ChatUtils.sendMessage(p, header("Pnjs"));
		for(CategoryPNJ pnj : pnjs.values()) {
			JRawMessage message = new JRawMessage("&c&l> ");
			String actual = PATTERN.replace("%name%", pnj.getName())
					.replace("%id%", pnj.getIdentifier())
					.replace("%loc%", pnj.locationToString());
			JRawMessage details = new JRawMessage(actual);
			details.addClickEvent(ClickEventType.RUN_COMMAND, "/tp " + pnj.locationToString(), false);
			
			JRawMessage edit = new JRawMessage("&2EDIT");
			edit.addClickEvent(ClickEventType.RUN_COMMAND, "/stand pnj edit " + pnj.getIdentifier(), false);
			
			JRawMessage del = new JRawMessage("&cDEL");
			del.addClickEvent(ClickEventType.RUN_COMMAND, "/stand pnj del " + pnj.getIdentifier(), false);
			
			JRawMessage open = new JRawMessage("&3OPEN");
			open.addClickEvent(ClickEventType.RUN_COMMAND, "/stand pnj open " + pnj.getIdentifier(), false);
			
			message.add(details, space, edit, space, del, space, open);
			
			message.send(p);
		}
		ChatUtils.sendMessage(p, footer("Pnjs"));
		
	}

	public void add(ApPNJ ap) {
		this.ap = ap;
		final Villager entity = (Villager) ap.createEntity();
		
		for(Entity e : entity.getNearbyEntities(1.0f, 1.0f, 1.0f)){
			if(entity.getCustomName().equals(e.getCustomName()))
				e.remove();
		}
		
		new FRunnable(40L){
			@Override
			public void run(){
				if(entity == null || entity.isDead()) {
					cancel(); return;
				}
				Location loc = ap.getLocation().clone();
				loc.setYaw(entity.getLocation().getYaw());
				loc.setPitch(entity.getLocation().getPitch());
				
				entity.teleport(loc);
			}
		}.start();
	}
	
}
