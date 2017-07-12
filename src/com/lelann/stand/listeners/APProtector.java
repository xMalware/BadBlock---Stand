package com.lelann.stand.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.lelann.factions.FactionObject;
import com.lelann.factions.api.FactionChunk;
import com.lelann.stand.StandPlugin;

public class APProtector extends FactionObject implements Listener {

	private StandPlugin plugin;
	private Map<Chunk, FactionChunk> protecteds = new HashMap<>();
	
	
	public APProtector(StandPlugin pl) {
		this.plugin = pl;
	}
	
	public APProtector register() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		return this;
	}
	
	public void protect(FactionChunk ap) {
		protecteds.put(ap.getChunk(), ap);
		ap.setProtected(true);
	}
	
	public void unprotect(FactionChunk ap) {
		protecteds.remove(ap.getChunk());
		ap.setProtected(false);
	}
	
	public boolean protectOf(Player p, Chunk in) {
		if(p.isOp() || p.hasPermission("faction.admin.bypass")) return false;
		if(protecteds.get(in) != null && protecteds.get(in).getOwner().getFactionId()
				== getPlayersManager().getPlayer(p).getFactionId()) return false;
		return protecteds.containsKey(in);
	}
	
	public boolean doProtect(Chunk c) {
		for(Chunk ch : protecteds.keySet()) {
			if(ch.getX() == c.getX() && ch.getZ() == c.getZ()) {
				return true;
			}
		}
		return protecteds.containsKey(c);
	}
	
	@EventHandler
	public void blockBreak(BlockBreakEvent e) {
		if(e.isCancelled()) return;
		e.setCancelled(protectOf(e.getPlayer(), e.getBlock().getChunk()));
	}
	
	@EventHandler
	public void blockPlace(BlockPlaceEvent e) {
		if(e.isCancelled()) return;
		e.setCancelled(protectOf(e.getPlayer(), e.getBlock().getChunk()));
	}
	
	@EventHandler
	public void explodeBlock(BlockExplodeEvent e) {
		if(e.isCancelled()) return;
		e.setCancelled(doProtect(e.getBlock().getChunk()));
	}
	
	@EventHandler
	public void explode(EntityExplodeEvent e) {
		if(e.isCancelled()) return;
		boolean affected = false;
		for(Block b : e.blockList()) {
			if(doProtect(b.getChunk())) {
				affected = true;
				break;
			}
		}
		//System.out.println("uhoh ! Explosion in " + c.getX() + ";" + c.getZ() + "... Protect it ? => " + affected);
		e.setCancelled(affected);
		/*List<Block> newList = new ArrayList<>();
		for(Block b : e.blockList()) {
			Chunk c = b.getChunk();
			if(!doProtect(c)) newList.add(b);
		}
		for(Block b : newList) {
			e.blockList().remove(b);
		}
		newList.clear();*/
		//e.setCancelled(protectOf(e.getEntity(), e.c));
	}
	
}
