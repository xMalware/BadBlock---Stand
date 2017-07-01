package com.lelann.stand.abstracts;

import java.util.UUID;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.projectiles.ProjectileSource;

import com.lelann.stand.StandPlugin;
import com.lelann.stand.objects.StandPlayer;

public abstract class StandObject {
	
	public static final String PREFIX = "%darkaqua%[%aqua%Stand%darkaqua%]&7 ";
	
	public String header(String what) {
		return "&8&l�&b&l-&8&l�&m----------&8&l�&b&l-&8&l�&b " + what + " &8&l�&b&l-&8&l�&m----------&f&8&l�&b&l-&8&l�&b";
	}
	
	public String footer(String what) {
		String toAdd = "";
		for(int i=0;i<what.length();i++) toAdd+="-";
		return "&8&l�&b&l-&8&l�&m-------------" + toAdd + "--------------&f&8&l�&b&l-&8&l�&b";
	}
	
	public StandPlugin getPlugin(){
		return StandPlugin.get();
	}
	
	public Server getServer(){
		return getPlugin().getServer();
	}
	
	public StandPlayer getPlayer(UUID uniqueId){
		return getPlugin().getPlayer(uniqueId);
	}
	
	public StandPlayer getPlayer(Player p){
		return getPlayer(p.getUniqueId());
	}
	
	public StandPlayer getPlayer(CommandSender sender){
		return (sender instanceof Player) ? getPlayer((Player) sender) : null;
	}
	
	public StandPlayer getPlayer(ProjectileSource source){
		return (source instanceof Player) ? getPlayer((Player) source) : null;
	}
	
	public StandPlayer getPlayer(Entity e){
		if(e instanceof Player){
			return getPlayer((Player) e);
		} else if(e instanceof Projectile){
			return getPlayer(((Projectile)e).getShooter());
		} else return null;
	}
	
	public StandPlayer getPlayer(PlayerEvent e){
		return getPlayer(e.getPlayer());
	}
	
	public StandPlayer getPlayer(EntityEvent e){
		return getPlayer(e.getEntity());
	}
}
