package com.lelann.stand.selection;

/**
 * Classe contenant plusieurs méthodes utiles pour l'utilisation des nombres
 * @author LeLanN
 */
public class MathsUtils {
	/**
	 * Ajoute un certains pourcentage à une valeur de base
	 * @param base La valeur de base
	 * @param percent Le pourcentage à ajouter
	 * @return La nouvelle valeur
	 */
	public static double addPercentage(double base, double percent){
		return (1d + percent / 100d) * base;
	}
	
	/**
	 * Ajoute un certains pourcentage, un certains nombre de fois, à une valeur de base
	 * @param base La valeur de base
	 * @param percent Le pourcentage à ajouter
	 * @param n Le nombre de fois où il faut ajouter ce pourcentage
	 * @return La nouvelle valeur
	 */
	public static double addPercentage(double base, double percent, int n){
		return Math.pow(1d + percent / 100d, n) * base;
	}
	
	/**
	 * Arrondit un nombre à un certains nombre de décimales
	 * @param number La valeur
	 * @param dec Le nombre de décimales
	 * @return La nouvelle valeur
	 */
	public static double round(double number, int dec){
		int div = (int) Math.pow(10, dec);
		return (double) ((int)(number * div)) / (double) div;
	}
	
	/**
	 * Récupère la plus petite valeur entre deux valeurs
	 * @param one La première
	 * @param scd La seconde
	 * @return La plus petite
	 */
	public static double min(double one, double scd){
		return one < scd ? one : scd;
	}
	
	/**
	 * Récupère la plus grande valeur entre deux valeurs
	 * @param one La première
	 * @param scd La seconde
	 * @return La plus grande
	 */
	public static double max(double one, double scd){
		return one > scd ? one : scd;
	}
}
