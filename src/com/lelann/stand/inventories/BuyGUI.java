package com.lelann.stand.inventories;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.ItemStack;

import com.lelann.factions.Main;
import com.lelann.factions.api.FactionPlayer;
import com.lelann.factions.api.jobs.JobManager;
import com.lelann.factions.utils.ItemUtils;
import com.lelann.stand.Requests;
import com.lelann.stand.events.ItemBoughtEvent;
import com.lelann.stand.inventories.abstracts.AbstractInventory;
import com.lelann.stand.inventories.abstracts.ClickableItem;
import com.lelann.stand.inventories.abstracts.InventoryManager;
import com.lelann.stand.objects.StandOffer;
import com.lelann.stand.objects.StandPlayer;

public class BuyGUI extends AbstractInventory {

	private StandOffer offer;
	private int quantity = 1;
	private StandPlayer viewer;
	
	private final double TAXE = 0.10;
	
	public BuyGUI(StandOffer offer, Player p) {
		super("&7Définissez la quantité que vous souhaitez", 9*3, p);
		this.offer = offer;
		this.viewer = getPlayer(p);
		setup();
	}
	
	public double taxe(int price) {
		if(Main.getInstance().getPlayersManager().getPlayer(viewer.getPlayer()).is("Vendeur")) {
			boolean bypass = (boolean) JobManager.getJob("Vendeur").getObject(Main.getInstance().getPlayersManager().getPlayer(viewer.getPlayer()), "bypass-taxes");
			if(bypass) {
				return 0;
			} else {
				return Math.round(((price * TAXE)) * 100) / 100.0;
			}
		}
		return Math.round(((price * TAXE)) * 100) / 100.0;
	}
	
	private void setup() {
		InventoryManager.addGui(this);
		topBar();
		setBottomBar(false, true);
		
		setupLeft(1, 2, 5, 10);
		setupRight(1, 2, 5, 10);
		
		setupItem();
		setupBuyItem();
	}
	
	private void setupBuyItem() {
		ItemStack newStack = ItemUtils.create("&aValider votre achat", new String[] {"&7Vous êtes sur le point d'acheter&b " + quantity + "&7 items",
				"&7Argent: &b" + viewer.getMoney() + "$",
				"&7Coût: &b" + (offer.getPrice() * quantity + taxe(offer.getPrice() * quantity)) + "$",
				"&7Après achat: &b" + (viewer.getMoney()-((offer.getPrice() * quantity) + taxe(offer.getPrice() * quantity))) + "$",
				"",
				"&7Cliquez pour acheter !"}, Material.BOOK);
		ItemAction action = new ItemAction() {
			
			@Override
			public void run(Player p, ItemStack clicked, int slot, InventoryAction action) {
				buy();
			}
		};
		
		editBottomBar(4, new ClickableItem(newStack, action));
	}
	
	private void updateBuyItem() {
		ItemStack newStack = ItemUtils.create("&aValider votre achat", new String[] {"&7Vous êtes sur le point d'acheter&b " + quantity + "&7 items",
				"&7Argent: &b" + viewer.getMoney() + "$",
				"&7Coût: &b" + (offer.getPrice() * quantity + taxe(offer.getPrice() * quantity)) + "$",
				"&7Après achat: &b" + (viewer.getMoney()-((offer.getPrice() * quantity) + taxe(offer.getPrice() * quantity))) + "$",
				"",
				"&7Cliquez pour acheter !"}, Material.BOOK);
		getInventory().setItem(getSize() - 9 + 4, newStack);
	}
	
	public boolean canGive() {
		ItemStack item = offer.getItem();
		item.setAmount(quantity);
		int maxStack = item.getMaxStackSize();
		int neededPlace = (item.getAmount() / maxStack) + (item.getAmount() % maxStack == 0 ? 0 : 1);
		int findedPlace = 0;

		for(int i=0;i<getPlayer().getInventory().getSize();i++){
			if(getPlayer().getInventory().getItem(i) == null){
				findedPlace++;
			}
		}
		
		if(findedPlace < neededPlace) return false;
		return true;
	}
	
	private void buy() {
		
		if(!canGive()) {
			viewer.sendMessage("&cVous n'avez pas assez de place dans votre inventaire !");
			return;
		}
		
		StandPlayer owner = offer.getPlayer(offer.getOwner());
		FactionPlayer p = Main.getInstance().getPlayersManager().getPlayer(offer.getOwner());
		
		if(owner.getUniqueId().equals(viewer.getUniqueId())) {
			viewer.sendMessage("&cT'es un marrant toi, en fait.");
			return;
		}
		
		long pricePlayer = (long) ((offer.getPrice() * quantity) + taxe(offer.getPrice() * quantity));
		int priceOwner = offer.getPrice() * quantity;
		
		if(viewer.getMoney() < pricePlayer) {
			viewer.sendMessage("&cVous n'avez pas assez d'argent !");
			return;
		}
		
		
		
		double more = 0.0D;
		String sellerMore = "";
		if(Main.getInstance().getPlayersManager().getPlayer(owner.getUniqueId()).is("Vendeur")) {
			double multiplicator = (double) JobManager.getJob("Vendeur").getObject(Main.getInstance().getPlayersManager().getPlayer(owner.getUniqueId()), "multiplicator");
			more = ((double) priceOwner * (double) multiplicator) - (double) priceOwner;

			sellerMore = "(&a+ " + more + "$ &7vendeur) ";
		}
		
		viewer.remove(pricePlayer);
		owner.add(priceOwner + (int) more);
		
		offer.remove(quantity);
		if(offer.getAmount() <= 0)
			owner.removeOffer(offer);
		
		if(owner != null) {
			owner.sendMessage(PREFIX + "Vous venez de vendre &a" + quantity + " " + offer.getName() + "&7 à &a" + viewer.getPlayer().getName() + "&7 pour &a" + priceOwner + "$&7 " + sellerMore + "!");
		}
		
		viewer.sendMessage(PREFIX + "Vous venez de d'acheter &a" + quantity + " " + offer.getName() + "&7 à &a" + p.getLastUsername() + "&7 pour &a" + pricePlayer + "$&7 !");
		
		ItemStack item = offer.getItem();
		item.setAmount(quantity);
		
		Requests.savePlayer(viewer);
		Requests.savePlayer(owner);
		
		Bukkit.getServer().getPluginManager().callEvent(new ItemBoughtEvent(viewer, offer, quantity));
		
		give(item);
		goBack();
	}
	
	private void setupItem() {
		ItemStack concerned = offer.createItemStack("&7Acheter &b" + quantity + "&7 unité" + (quantity > 1 ? "s" : ""), "&7Cliquez pour acheter &b" + quantity + " " + offer.getName(), "&7en la quantité voulue !", "", "&7Coût: &b" + offer.getPrice() * quantity, "&7Taxe:&b " + taxe(offer.getPrice() * quantity));
		concerned.setAmount(quantity);
		
		ClickableItem item = new ClickableItem(concerned, null);
		addClickable(13, item);
	}
	
	private void updateItem() {
		ItemStack concerned = offer.createItemStack("&7Acheter &b" + quantity + "&7 unité" + (quantity > 1 ? "s" : ""), "&7Cliquez pour acheter &b" + quantity + " " + offer.getName(), "&7en la quantité voulue !", "", "&7Coût: " + offer.getPrice() * quantity, "&7Taxe:&b " + taxe(offer.getPrice() * quantity));
		concerned.setAmount(quantity);
		getInventory().setItem(13, concerned);
	}
	
	private void setupLeft(int... q) {
		int startSlot = 12; // 8 + 4
		for(int slot = 0; slot < q.length; slot++) {
			int quantity = q[slot];
			int currentSlot = startSlot-slot;
			
			ItemStack quant = ItemUtils.create("&c-" + quantity, new String[] {"&7Enlève &b" + quantity + "&7 unité" + (quantity > 1 ? "s" : "")}, Material.STAINED_GLASS_PANE, quantity, (short) 14);
			ItemAction action = new ItemAction() {
				
				@Override
				public void run(Player p, ItemStack clicked, int slot, InventoryAction action) {
					setQuantity(slot, false);
					
				}
			};
			
			ClickableItem clickable = new ClickableItem(quant, action);
			addClickable(currentSlot, clickable);
		}
	}

	private void setupRight(int... q) {
		int startSlot = 14; // 8 + 6
		for(int slot = 0; slot < q.length; slot++) {
			int quantity = q[slot];
			int currentSlot = startSlot+slot;
			
			ItemStack quant = ItemUtils.create("&a+" + quantity, new String[] {"&7Ajoute &b" + quantity + "&7 unité" + (quantity > 1 ? "s" : "")}, Material.STAINED_GLASS_PANE, quantity, (short) 13);
			ItemAction action = new ItemAction() {
				
				@Override
				public void run(Player p, ItemStack clicked, int slot, InventoryAction action) {
					setQuantity(slot, true);
				}
			};
			
			ClickableItem clickable = new ClickableItem(quant, action);
			addClickable(currentSlot, clickable);
		}
	}
	
	private void setQuantity(int slot, boolean add) {
		
		if(quantity <= 1 && !add) {
			return;
		}
		
		if(quantity >= offer.getAmount() && add) {
			return;
		}
		
		int quantity = getInventory().getItem(slot).getAmount();
		
		if(this.quantity + quantity > offer.getAmount() && add) {
			return;
		}
		
		if(this.quantity - quantity < 1 && !add) {
			return;
		}
		
		if(add)
			this.quantity += quantity;
		else
			this.quantity -= quantity;
		
		updateItem();
		updateBuyItem();
	}
	
	private void topBar() {
		for(int slot = 0; slot < 9; slot++) {
			addSeparator(slot);
		}
	}

	@Override
	public boolean onClick(Player p, ItemStack clicked, ItemStack cursor, int slot, InventoryAction action,
			ClickType clickType, SlotType slotType) {
		return true;
	}

	@Override
	public void onClose(Player p) {
		
	}

	
	
}
