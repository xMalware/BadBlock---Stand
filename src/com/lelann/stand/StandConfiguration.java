package com.lelann.stand;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.lelann.stand.selection.CuboidSelection;
import com.lelann.stand.selection.SelectionUtils;
import com.lelann.stand.selection.Vector3f;

import lombok.Getter;

public class StandConfiguration {
	private ConfigurationSection config;
	
	@Getter private static StandConfiguration instance;
	@Getter private List<String> allowedOfferNumber;
	@Getter private List<CuboidSelection> disallowedZones;
	
	public int allowedOfferNumber(Player p){
		int allowed = 0;
		for(String offerNumber : allowedOfferNumber){
			try {
				String[] splitted = offerNumber.split(":");
				int number = Integer.parseInt(splitted[1]);
				
				if(p.hasPermission(splitted[0]) && number > allowed){
					allowed = Integer.parseInt(splitted[1]);
				}
			} catch(Exception unused){}
		}
		
		allowed += allowed % 9 == 0 ? 0 : 9 - (allowed % 9);
	
		if(allowed > 54){ allowed = 54; } 
		else if(allowed < 9){ allowed = 9; }
		
		return allowed;
	}
	
	@SuppressWarnings("serial")
	public StandConfiguration(ConfigurationSection config){
		instance = this;
		this.config = config;
		
		allowedOfferNumber = get("allowedOfferNumber", new ArrayList<String>(){{add("stand.play.maxOffer.1:9");add("stand.play.maxOffer.2:54");}});
		
		if(!config.contains("disallowedZonesForStandPlace")){
			config.createSection("disallowedZonesForStandPlace");
		}
		
		ConfigurationSection section = config.getConfigurationSection("disallowedZonesForStandPlace");
		disallowedZones = new ArrayList<>();
		
		for(String key : section.getKeys(false)){
			ConfigurationSection sel = section.getConfigurationSection(key);
			if(sel == null) continue;
			
			CuboidSelection selection = SelectionUtils.load(sel);
			disallowedZones.add(selection);
		}
		
		if(disallowedZones.isEmpty()){
			ConfigurationSection sel = section.createSection("example");
			SelectionUtils.save(sel, new CuboidSelection(Bukkit.getWorlds().get(0).getName(), new Vector3f(), new Vector3f()));
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(String key, T defaultValue){
		if(!config.contains(key))
			config.set(key, defaultValue);
		if(!(config.get(key).getClass().isInstance(defaultValue)))
			config.set(key, defaultValue);
		return (T) config.get(key);
	}
}
