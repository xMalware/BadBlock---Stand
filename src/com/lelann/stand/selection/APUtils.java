package com.lelann.stand.selection;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class APUtils {

	public static Location getTpLoc(String world, int chunkX, int chunkZ) {
		int realX = 0;
		int realZ = 0;
		if(Math.abs(chunkX) == Math.abs(chunkZ)) {
			if(chunkX < 0) {
				chunkX++;
				realX = chunkX * 16 + 2;
			} else {
				chunkX--;
				realX = chunkX * 16 + (16-3);
			}
			if(chunkZ < 0) {
				chunkZ++;
				realZ = chunkZ * 16 + 2;
			} else {
				chunkZ--;
				realZ = chunkZ * 16 + (16-3);
			}
		} else {
			if(chunkX == 19) {
				chunkX--;
				realZ = chunkZ * 16 + 8;
				realX = chunkX * 16 + (16-3);
			} else if(chunkX == -19) {
				chunkX++;
				realZ = chunkZ * 16 + 8;
				realX = chunkX * 16 + 2;
			} else if(chunkZ == 19) {
				chunkZ--;
				realX = chunkX * 16 + 8;
				realZ = chunkZ * 16 + (16-3);
			} else if(chunkZ == -19) {
				chunkZ++;
				realX = chunkX * 16 + 8;
				realZ = chunkZ * 16 + 2;
			}
		}
		//return realX + " " + getDispoY(world, realX, realZ) + " " + realZ;
		return new Location(Bukkit.getWorld(world), realX, getDispoY(world, realX, realZ), realZ);
	}
	
	public static int getDispoY(String world, int x, int z) {
		World w = Bukkit.getWorld(world);
		for(int y = 0; y < w.getMaxHeight(); y++) {
			Block b1 = w.getBlockAt(x, y, z);
			Block b2 = w.getBlockAt(x, y+1, z);
			
			if(b1.getType() == Material.AIR && b2.getType() == Material.AIR)
				return y;
			
		}
		return w.getMaxHeight();
	}
	
}
