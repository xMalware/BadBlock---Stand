package com.lelann.stand;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;

import com.lelann.factions.Main;
import com.lelann.factions.api.Faction;
import com.lelann.factions.api.FactionChunk;
import com.lelann.factions.api.managers.ChunksManager;
import com.lelann.stand.commands.CommandsManager;
import com.lelann.stand.inventories.abstracts.Categories;
import com.lelann.stand.listeners.APProtector;
import com.lelann.stand.listeners.ApPNJListener;
import com.lelann.stand.listeners.ApPNJManager;
import com.lelann.stand.listeners.CategoryPNJListener;
import com.lelann.stand.listeners.CategoryPNJManager;
import com.lelann.stand.listeners.ChatListener;
import com.lelann.stand.listeners.GeneralListener;
import com.lelann.stand.listeners.GuiListener;
import com.lelann.stand.listeners.StandListener;
import com.lelann.stand.objects.APOffer;
import com.lelann.stand.objects.StandFaction;
import com.lelann.stand.objects.StandPlayer;

import lombok.Getter;

public class StandPlugin extends JavaPlugin {
	
	private static StandPlugin instance = null;
	public static StandPlugin get() {
		return instance;
	}

	private Map<UUID, ArmorStand> stands;
	private Map<UUID, StandPlayer> players;
	private Map<Integer, StandFaction> factions = new HashMap<>();
	
	@Getter private APProtector protector;

	@Getter private File pnj;
	
	@Getter CategoryPNJManager manager;
	@Getter ApPNJManager APManager;

	public ArmorStand getArmorStand(UUID uniqueId){
		return stands.get(uniqueId);
	}

	public void setArmorStand(UUID uniqueId, ArmorStand stand){
		stands.put(uniqueId, stand);
	}

	public void removeArmorStand(UUID uniqueId){
		ArmorStand stand = getArmorStand(uniqueId);
		if(stand != null){
			stand.remove();
			stands.remove(uniqueId);
		}
	}

	public StandPlayer getPlayer(UUID uniqueId){
		return players.get(uniqueId);
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

	public void connect(Player p){
		StandPlayer player = getPlayer(p);
		if(player == null){
			players.put(p.getUniqueId(), new StandPlayer(p));
		}
		getPlayer(p).sendRequestMessages();
	}

	@Override
	public void onEnable(){
		instance = this;

		pnj = new File(getDataFolder(), "pnjs.yml");
		players = new HashMap<UUID, StandPlayer>();
		stands = new HashMap<UUID, ArmorStand>();

		FileConfiguration config = YamlConfiguration.loadConfiguration(pnj);

		//List<StandTopPNJ> pnjs =  new ArrayList<StandTopPNJ>();
		List<StandPlayer> players =  new ArrayList<StandPlayer>();

		if(config.getConfigurationSection("Pnjs") == null){
			config.createSection("Pnjs");
			try {
				config.save(pnj);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		manager = new CategoryPNJManager(Categories.loadCategories(pnj));
		APManager = new ApPNJManager(Categories.loadAP());
		protector = new APProtector(this).register();
		
		/*for(String key : config.getConfigurationSection("Pnjs").getKeys(false)){
			try {
				pnjs.add(new StandTopPNJ(config.getConfigurationSection("pnjs." + key)));
			} catch(Exception e){
				e.printStackTrace();
			}
		}*/
		
		System.out.println("Loading stands");
				
		//String request = "SELECT uniqueId, standName, standRemove, loc, sOffers.id, type, data, itemstack, amount, price, owner  FROM sPlayers INNER JOIN sOffers ON sPlayers.uniqueId = sOffers.owner";
		String request = "SELECT * FROM sPlayers";
		
		try {
			ResultSet set = Main.getInstance().getDB().querySQL(request);
			while(set.next()){
				UUID uniqueId = UUID.fromString(set.getString("uniqueId"));
				StandPlayer player = null;

				if(StandPlugin.this.players.containsKey(uniqueId)){
					player = StandPlugin.this.players.get(uniqueId);
				} else {
					player = new StandPlayer(set, true);
						
					if(player.getStandRemove() != 0 && player.getStandLoc() != null){
						players.add(player);
					} else {
						player.setStandRemove(0);
						player.setStandLoc(null);
					}

					StandPlugin.this.players.put(player.getUniqueId(), player);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println(StandPlugin.this.players.size() + " players loaded !");
		System.out.println(StandPlayer.allOffers.size() + " offers loaded !");
		System.out.println(StandPlayer.allRequests.size() + " requests loaded !");
		
		for(Faction f : Main.getInstance().getFactionsManager().getLoadedFactions().values()) {
			addStandFaction(new StandFaction(f));
		}
		
		System.out.println(StandFaction.allOffers.size() + " apoffers loaded !");
		System.out.println(StandFaction.allRequests.size() + " aprequests loaded !");
		
		int p = 0;
		for(ChunksManager manager : Main.getInstance().getChunksManagers()) {
			for(FactionChunk chunk : manager.getChunks().values()) {
				if(chunk.isProtected()) {
					protector.protect(chunk);
					p++;
				}
			}
		}
		
		System.out.println(p + " APs protections loaded !");
		
		new StandConfiguration(getConfig());
		saveConfig();

		getServer().getPluginManager().registerEvents(new GuiListener(), this);
		getServer().getPluginManager().registerEvents(new CategoryPNJListener(), this);
		getServer().getPluginManager().registerEvents(new ApPNJListener(), this);
		getServer().getPluginManager().registerEvents(new StandListener(players), this);
		getServer().getPluginManager().registerEvents(new GeneralListener(), this);
		getServer().getPluginManager().registerEvents(new ChatListener(), this);
		
		new CommandsManager();
	}

	@Override
	public void onDisable() { }

	@Override
	public void onLoad() {
		//		new ThreadWG().start();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		CommandsManager.getInstance().useCommand(sender, args);
		return true;
	}

	public void addStandFaction(StandFaction faction) {
		factions.put(faction.getFaction().getFactionId(), faction);
	}
	
	public void removeStandFaction(Faction faction) {
		StandFaction fac = getStandFaction(faction);
		fac.deleteAll();
		factions.remove(fac);
	}
	
	public StandFaction getStandFaction(Faction faction) {
		return factions.get(faction.getFactionId());
	}

	public void sellAp(Faction f, FactionChunk chunk, int price) {
		StandFaction faction = getStandFaction(f);
		Main.getInstance().getChunksManager(chunk.getWorld()).setOnSale(chunk, true);
		Main.getInstance().getChunksManager(chunk.getWorld()).saveChunk(chunk, true);
		
		protector.protect(chunk);
		
		APOffer offer = new APOffer(f, chunk, price);
		faction.addOffer(offer);
		faction.save();
		faction.getFaction().save(false);
	}
	
	public void unsellAp(Faction f, APOffer offer) {
		StandFaction faction = getStandFaction(f);
		FactionChunk chunk = offer.getAp();
		faction.removeOffer(offer);
		Main.getInstance().getChunksManager(chunk.getWorld()).setOnSale(chunk, false);
		Main.getInstance().getChunksManager(chunk.getWorld()).saveChunk(chunk, true);
		
		protector.unprotect(chunk);
		
		faction.save();
		faction.getFaction().save(false);
	}
}
