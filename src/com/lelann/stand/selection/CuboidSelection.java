package com.lelann.stand.selection	;

import com.lelann.factions.utils.MathsUtils;

import lombok.Getter;

/**
 * Représente une séléction en forme de cube, définie par deux vecteurs et un monde
 * @author LeLanN
 */
public class CuboidSelection extends AbstractSelection {
	@Getter private Vector3f firstBound, secondBound;
	
	/**
	 * Crée une nouvelle séléction à partir du nom du monde et deux de points
	 * @param worldName Le monde
	 * @param firstBound Le premier vecteur
	 * @param secondBound Le seconde vecteur
	 */
	public CuboidSelection(String worldName, Vector3f firstBound, Vector3f secondBound){
		super(worldName);
		this.firstBound = firstBound;
		this.secondBound = secondBound;
	}
	
	@Override
	public boolean isInSelection(Vector3f loc) {
		return loc.getX() >= MathsUtils.min(firstBound.getX(), secondBound.getX())
				&& loc.getX() <= MathsUtils.max(firstBound.getX(), secondBound.getX())
				&& loc.getY() >= MathsUtils.min(firstBound.getY(), secondBound.getY())
				&& loc.getY() <= MathsUtils.max(firstBound.getY(), secondBound.getY())
				&& loc.getZ() >= MathsUtils.min(firstBound.getZ(), secondBound.getZ())
				&& loc.getZ() <= MathsUtils.max(firstBound.getZ(), secondBound.getZ());
	}
}
