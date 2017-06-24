package com.lelann.stand.selection;

import org.bukkit.configuration.ConfigurationSection;

public class SelectionUtils {
	public static CuboidSelection load(ConfigurationSection c){
		Vector3f min = new Vector3f(c.getDouble("min.x")
				, c.getDouble("min.y")
				, c.getDouble("min.z"));
		Vector3f max = new Vector3f(c.getDouble("max.x")
				, c.getDouble("max.y")
				, c.getDouble("max.z"));
		
		String world = c.getString("world");
		
		return new CuboidSelection(world, min, max);
	}
	
	public static void save(ConfigurationSection c, CuboidSelection s){
		c.set("min.x", s.getFirstBound().getX());
		c.set("min.y", s.getFirstBound().getY());
		c.set("min.z", s.getFirstBound().getZ());
	
		c.set("max.x", s.getSecondBound().getX());
		c.set("max.y", s.getSecondBound().getY());
		c.set("max.z", s.getSecondBound().getZ());
		
		c.set("world", s.getWorldName());	
	}
}
