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
import com.lelann.stand.commands.CommandsManager;
import com.lelann.stand.inventories.abstracts.Categories;
import com.lelann.stand.listeners.CategoryPNJListener;
import com.lelann.stand.listeners.CategoryPNJManager;
import com.lelann.stand.listeners.GeneralListener;
import com.lelann.stand.listeners.GuiListener;
import com.lelann.stand.listeners.StandListener;
import com.lelann.stand.objects.StandOffer;
import com.lelann.stand.objects.StandPlayer;

import lombok.Getter;

public class StandPlugin extends JavaPlugin {
	private static StandPlugin instance = null;
	public static StandPlugin get(){
		return instance;
	}

	private Map<UUID, ArmorStand> stands;
	private Map<UUID, StandPlayer> players;

	@Getter private File pnj;
	
	@Getter CategoryPNJManager manager;

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
		
		new StandConfiguration(getConfig());
		saveConfig();

		getServer().getPluginManager().registerEvents(new GuiListener(), this);
		getServer().getPluginManager().registerEvents(new CategoryPNJListener(), this);
		getServer().getPluginManager().registerEvents(new StandListener(players), this);
		getServer().getPluginManager().registerEvents(new GeneralListener(), this);

		new CommandsManager();
	}

	@Override
	public void onDisable(){}

	@Override
	public void onLoad() {
		//		new ThreadWG().start();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		CommandsManager.getInstance().useCommand(sender, args);
		return true;
	}

	//	public void load() {
	//		WorldGuardManager manager = new WorldGuardManager();
	//		if(manager.getFlag("standzone") == null) {
	//			manager.addFlag("standzone", false);
	//		}
	//	}

	//	public class ThreadWG extends Thread {
	//		public ThreadWG() {}
	//
	//		public void run() {
	//			while(StandPlugin.this.getServer().getPluginManager().getPlugin("WorldGuard") == null) {
	//				try {
	//					Thread.sleep(5);
	//				} catch (InterruptedException e) {
	//					e.printStackTrace();
	//				}
	//			}
	//			StandPlugin.this.load();
	//		}
	//	}
}
