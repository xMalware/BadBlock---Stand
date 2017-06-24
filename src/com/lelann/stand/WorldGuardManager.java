package com.lelann.stand;

import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WorldGuardManager {
	private static WorldGuardManager instance = null;
	public static WorldGuardManager get(){
		if(instance == null) instance = new WorldGuardManager();
		return instance;
	}
	
	/* Management de régions */
	public void addRegion(String name, Location l1, Location l2){
		ProtectedRegion r = new ProtectedCuboidRegion(name, getBlockVector(l1), getBlockVector(l2));
		getWorldGuard().getRegionManager(l1.getWorld()).addRegion(r);
	}

	public void removeRegion(String region, World w){
		if(hasRegion(region, w)){
			getWorldGuard().getRegionManager(w).removeRegion(region);
		}
	}

	public ProtectedCuboidRegion getRegion(String region, World w){
		ProtectedRegion r = getWorldGuard().getRegionManager(w).getRegion(region);
		if(r == null) return null;
		if(r instanceof ProtectedCuboidRegion){
			return (ProtectedCuboidRegion)r;
		} else return null;
	}

	public boolean hasRegion(String region, World w){
		return getWorldGuard().getRegionManager(w).hasRegion(region);
	}

	public ProtectedCuboidRegion resizeRegion(ProtectedCuboidRegion r, Location l1, Location l2){
		r.setMinimumPoint(getBlockVector(l1));
		r.setMaximumPoint(getBlockVector(l2));

		return r;
	}

	/* Management de flags */
	@SuppressWarnings("rawtypes")
	public StateFlag getFlag(String flag){
		for(Flag f : DefaultFlag.getFlags()){
			if(f.getName().equalsIgnoreCase(flag) && f instanceof StateFlag)
				return (StateFlag)f;
		}
		return null;
	}
	@SuppressWarnings("rawtypes")
	public void addFlag(String flagName, boolean def){
		try
		{
			Field flagField = DefaultFlag.class.getField("flagsList");

			Flag flags[] = new Flag[DefaultFlag.flagsList.length + 1];
			System.arraycopy(DefaultFlag.flagsList, 0, flags, 0, DefaultFlag.flagsList.length);
			flags[DefaultFlag.flagsList.length] = new StateFlag(flagName, def);

			Field modifier = Field.class.getDeclaredField("modifiers");
			modifier.setAccessible(true);
			modifier.setInt(flagField, flagField.getModifiers() & 0xffffffef);
			flagField.set(null, flags);
		} catch(Exception ex){}
	} 
	@SuppressWarnings("rawtypes")
	public void removeFlag(String flagName){
		if(getFlag(flagName) == null) return;
		try{
			Field flagField = DefaultFlag.class.getField("flagsList");

			Flag flags[] = new Flag[DefaultFlag.flagsList.length - 1];
			int i = 0;
			for(Flag f : DefaultFlag.flagsList){
				if(!f.getName().equalsIgnoreCase(flagName)){
					flags[i] = f;
					i++;
				}
			}
			System.arraycopy(DefaultFlag.flagsList, 0, flags, 0, DefaultFlag.flagsList.length);

			Field modifier = Field.class.getDeclaredField("modifiers");
			modifier.setAccessible(true);
			modifier.setInt(flagField, flagField.getModifiers() & 0xffffffef);
			flagField.set(null, flags);
		} catch(Exception ex){}
	}

	/* Général */
	public BlockVector getBlockVector(Location l){
		return new BlockVector(l.getX(), l.getY(), l.getZ());
	}
	public WorldGuardPlugin getWorldGuard() {
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");

		if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
			return null;
		}

		return (WorldGuardPlugin) plugin;
	}

	/* Vérification */
	public boolean hasFlag(String flag, Location l){
		return getWorldGuard().getGlobalRegionManager().allows(getFlag(flag), l);
	}
}