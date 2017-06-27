package com.lelann.stand.objects;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_8_R3.PacketPlayOutSetSlot;
import net.minecraft.server.v1_8_R3.PacketPlayOutWindowItems;

public class Packets {

	public static PacketPlayOutWindowItems sendItemUpdate(Player player, ItemStack[] item)
    {
 
		List<net.minecraft.server.v1_8_R3.ItemStack> stacks = Arrays.asList(toItemStack(item));
		PacketPlayOutWindowItems packet = new PacketPlayOutWindowItems(0, stacks);
        return packet;
   
    }
 
    public static net.minecraft.server.v1_8_R3.ItemStack[] toItemStack(ItemStack[] item)
    {
   
    	net.minecraft.server.v1_8_R3.ItemStack[] serverItems = new net.minecraft.server.v1_8_R3.ItemStack[item.length];
   
        for (int i = 0; i < item.length; i ++)
        {
       
            serverItems[i] = CraftItemStack.asNMSCopy(item[i]);
       
        }
   
        return serverItems;
   
    }
    
    public static void updateInventory(Player p, Inventory inv) {
        CraftPlayer c = (CraftPlayer) p;
        for (int i = 0;i < inv.getSize();i++) {
            int nativeindex = i;
            //if (i < 9) nativeindex = i + 36;
            ItemStack olditem =  inv.getItem(i);
            net.minecraft.server.v1_8_R3.ItemStack item = null;
            if (olditem != null && olditem.getType() != Material.AIR) {
                item = CraftItemStack.asNMSCopy(olditem);
            }
            PacketPlayOutSetSlot pack = new PacketPlayOutSetSlot(0, nativeindex, item);
            c.getHandle().playerConnection.sendPacket(pack);
        }
        for (int i = 0; i < c.getInventory().getSize();i++) {
            int nativeindex = i;
            if (i < 9) nativeindex = i + 36;
            ItemStack olditem =  c.getInventory().getItem(i);
            net.minecraft.server.v1_8_R3.ItemStack item = null;
            if (olditem != null && olditem.getType() != Material.AIR) {
                item = CraftItemStack.asNMSCopy(olditem);
            }
            PacketPlayOutSetSlot pack = new PacketPlayOutSetSlot(0, nativeindex, item);
            c.getHandle().playerConnection.sendPacket(pack);
        }
    }
	
}
