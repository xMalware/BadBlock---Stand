package com.lelann.stand.selection;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import lombok.Getter;

/**
 * Repr�sente une zone dans l'espace d�finie par un nombre inconnu de vecteurs et un monde
 * @author LeLanN
 */
public abstract class AbstractSelection {
	@Getter protected String worldName;
	
	public AbstractSelection(String worldName){
		this.worldName = worldName;
	}
	
	/**
	 * V�rifie si le vecteur est contenu dans la s�l�ction
	 * @param loc Le vecteur
	 * @return Un boolean
	 */
	public abstract boolean isInSelection(Vector3f loc);

	/**
	 * V�rifie si une location est dans la s�l�ction (monde et coordonn�es)
	 * @param loc La location
	 * @return Un boolean
	 */
	public boolean isInSelection(Location loc){
		return loc.getWorld().getName().equalsIgnoreCase(worldName) 
				&& isInSelection(new Vector3f(loc));
	}
	
	/**
	 * V�rifie si une entit� est dans la s�l�ction (monde et coordonn�es)
	 * @param e L'entit�
	 * @return Un boolean
	 */
	public boolean isInSelection(Entity e){
		return isInSelection(e.getLocation());
	}
	
	/**
	 * V�rifie si un bloc est dans la s�l�ction (monde et coordonn�es)
	 * @param e L'entit�
	 * @return Un boolean
	 */
	public boolean isInSelection(Block b){
		return isInSelection(b.getLocation());
	}
	
	/**
	 * V�rifie si trois points sont dans la ss�l�ction
	 * @param x X
	 * @param y Y
	 * @param z Z
	 * @return Un boolean
	 */
	public boolean isInSelection(double x, double y, double z){
		return isInSelection(new Vector3f(x, y, z));
	}
}
