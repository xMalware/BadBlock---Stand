package com.lelann.stand.commands;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.lelann.factions.FactionObject;
import com.lelann.factions.api.Faction;
import com.lelann.factions.api.FactionChunk;
import com.lelann.factions.api.managers.ChunksManager;
import com.lelann.factions.utils.Title;
import com.lelann.stand.StandConfiguration;
import com.lelann.stand.listeners.StandListener;
import com.lelann.stand.objects.StandPlayer;
import com.lelann.stand.objects.StandPlayer.StandAction;
import com.lelann.stand.selection.CuboidSelection;

public class Place extends AbstractCommand {
	public Place() {
		super("place", "stand.play.place", "&c&l>&7 /stand place", "&7Place un PNJ à votre place et vous déconnecte.", "/stand place", null);
	}

	@Override
	public void runCommand(CommandSender sender, String[] args) {
		if(!(sender instanceof Player)){
			sendMessage(sender, NO_CONSOLE);
			return;
		}

		Player p = (Player) sender;
		if(!canPlace(p)){
			sendMessage(p, "%red%Vous ne pouvez pas placer votre stand ici !");
		} else {
			StandPlayer player = getPlayer(p);
			if(player.getAction() == StandAction.WAITING_KICK) return;
			if(player.getOffers().isEmpty()){
				sendMessage(p, "%red%Vous n'avez aucune offre à vendre !"); return;
			}
			
			for(Entity e : p.getNearbyEntities(1.5, 1.5, 1.5)){
				if(e.getType() == EntityType.ARMOR_STAND){
					sendMessage(p, "%red%Veuillez vous éloigner un minimum des autres stands !"); return;
				}
			}
			
			StandListener.getInstance().add(p.getLocation(), p, player);
//			player.place(p.getName(), p.getItemInHand(), p.getLocation());
			player.setAction(StandAction.WAITING_KICK);
			
			final UUID uniqueId = p.getUniqueId();
			
			new BukkitRunnable(){
				private int time = 10;
				@Override
				public void run(){
					Player p = getServer().getPlayer(uniqueId);
					if(p == null){
						cancel(); return;
					} else if(time == 0){
						cancel(); send(p);
					} else {
						new Title("&c" + time, "&cDéconnection dans " + time + " seconde" + (time > 1 ? "s" : "") + " !").send(p);
					}
					
					time--;
				}
			}.runTaskTimer(getPlugin(), 0, 20L);
		}
	}
	
	public boolean canPlace(Player p){
		FactionObject o = new FactionObject();
		boolean can = false;
		
		if(!o.getPlayersManager().getPlayer(p).isBypass()){
			ChunksManager cm = o.getChunksManager(p.getLocation().getChunk().getWorld());
			if(cm != null) {
				FactionChunk fChunk = cm.getFactionChunk(p.getLocation().getChunk());
				if(fChunk != null && fChunk.getFactionId() == Faction.SAFEZONE.getFactionId()){
					can = true;
				}
			}
		}
		if(!can) return false;
		
		for(CuboidSelection selection : StandConfiguration.getInstance().getDisallowedZones()){
			if(selection.isInSelection(p))
				return false;
		}
		
		return true;
	}
	
	public void send(Player p){
		p.kickPlayer("");
	}
}