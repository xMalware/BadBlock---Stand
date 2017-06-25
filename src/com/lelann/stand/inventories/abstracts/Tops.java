package com.lelann.stand.inventories.abstracts;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.lelann.stand.inventories.TopGUI;

public class Tops {
	
	/**
	 * Ouvre l'inventaire affichante le top des vendeurs (moins et plus chers)
	 * @param p Le joueur pour qui ouvrir l'inventaire
	 * @param item L'item pour lequel on recherche le top
	 */
	public static void openTopGui(Player p, ItemStack item) {
		getTopGui(item).showBefore(p);
	}
	
	private static TopGUI getTopGui(ItemStack item) {
		return new TopGUI("&7Voir les prix de ventes", item);
	}
	
}
