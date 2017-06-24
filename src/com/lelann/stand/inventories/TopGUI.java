package com.lelann.stand.inventories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.lelann.factions.database.Callback;
import com.lelann.factions.utils.ChatUtils;
import com.lelann.factions.utils.ItemUtils;
import com.lelann.stand.Requests;
import com.lelann.stand.StandPlugin;
import com.lelann.stand.inventories.abstracts.AbstractInventory;
import com.lelann.stand.inventories.abstracts.ClickableItem;
import com.lelann.stand.inventories.abstracts.InventoryManager;
import com.lelann.stand.inventories.items.MenuItem;
import com.lelann.stand.objects.StandOffer;
import com.lelann.stand.objects.StandPlayer;

public class TopGUI extends AbstractInventory {

	private ItemStack item;
	private boolean hasOffers = true;
	
	private final double TAXE = 0.10;
	
	private int totalMoney = 0;
	private int totalStack = 0;
	private List<StandOffer> toBuy = new ArrayList<>();
	private Map<StandOffer, Integer> amounts = new HashMap<>();
	
	public TopGUI(String title, ItemStack item) {
		super(title);
		this.item = item;
		setup();
	}

	public void setup() {
		setBottomBar(true, true);
		ClickableItem book = new ClickableItem(ItemUtils.create("&aValider votre achat", new String[] {"&cVous devez au moins avoir un item dans votre panier", "&cpour finaliser votre achat !"}, Material.BOOK), new ItemAction() {
			
			@Override
			public void run(Player p, ItemStack clicked, int slot, InventoryAction action) {	
				if(totalStack > 0) {
					buySelectedItems();
				} else {
					ChatUtils.sendMessage(p, "&cVous n'avez pas séléctionné d'items !");
				}
			}
		});
		editBottomBar(4, book);
	}
	
	public void updateBuyItem(ItemStack newItem) {
		editBottomBar(4, getClickable(4).update(newItem));
	}
	
	public void updateBuyItem(String name, String[] description) {
		editBottomBar(4, getClickable(4).update(ItemUtils.create(getInventory().getItem(4), name, description)));
	}
	
	public void updateBuyItem(ClickableItem item) {
		editBottomBar(4, item);
	}
	
	public void updateBuyItem() {
		StandPlayer player = StandPlugin.get().getPlayer(getPlayer());
		ItemStack newStack = null;
		
		if(totalStack > 0) {
			newStack = ItemUtils.create("&aValider votre achat", new String[] {"&7Vous êtes sur le point d'acheter&b" + totalStack + "&7 items",
				"&7Argent:      &b" + player.getMoney() + "$",
				"&7Coût:        &b" + totalMoney + "$",
				"&7Après achat: &b" + (player.getMoney()-totalMoney),
				"",
				"&7Cliquez pour acheter !"}, Material.BOOK);
		} else {
			newStack = ItemUtils.create("&aValider votre achat", new String[] {"&cVous devez au moins avoir un item dans votre panier", "&cpour finaliser votre achat !"}, Material.BOOK);
		}
		editBottomBar(4, getClickable(4).update(newStack));
	}
	
	private void buySelectedItems() {
		if(toBuy.isEmpty()) {
			ChatUtils.sendMessage(getPlayer(), "&cVous devez séléctionner au moins un item à acheter !");
			return;
		}
		
		StandPlayer player = StandPlugin.get().getPlayer(getPlayer());
		
		if(totalMoney > player.getMoney()) {
			player.sendMessage("&cVous ne disposez pas d'assez d'argent pour effectuer cette commande !");
			return;
		}
		
		if(!canGiveAll()) {
			player.sendMessage("&cVous n'avez pas assez de place dans l'inventaire. Commencez par en faire avant de retenter l'achat.");
			return;
		}
		
		for(StandOffer buying : amounts.keySet()) {
			
			if(amounts.get(buying) < 0) continue;
			
			StandPlayer owner = buying.getPlayer(buying.getOwner());
			
			int amount = amounts.get(buying);
			int priceForPlayer = (int) Math.floor((amount * buying.getPrice()) * TAXE);
			int priceForOwner = amount * buying.getPrice();
			
			//removing money and adding items
			player.remove(priceForPlayer);
			owner.add(priceForOwner);
			
			buying.remove(amount);
			
			if(buying.getAmount() <= 0){
				owner.removeOffer(buying);
			}
			
			if(owner.getPlayer() != null){
				owner.sendMessage("&a[Stand] Vous avez vendu " + amount + " " + buying.getType().name().toLowerCase().replace("_", " ") + " à " + player.getPlayer().getDisplayName() + " &apour " + priceForOwner + "$");
			}
			
			ItemStack item = buying.getItem();
			item.setAmount(amount);
			
			player.getPlayer().getInventory().addItem(item);
			
			Requests.savePlayer(owner);
			//getPlayer().closeInventory();
		}
		
		getPlayer().closeInventory();
		
	}
	
	private void addItemToCart(int slot, StandOffer offer) {
		if(getInventory().getItem(slot).getAmount() < offer.getAmount()) {
			totalStack++;
			totalMoney+=offer.getPrice() + Math.floor(offer.getPrice() * TAXE);
			amounts.put(offer, getInventory().getItem(slot).getAmount());
			indicator(slot, true);
			updateBuyItem();
		}
	}
	
	private void removeItemFromCart(int slot, StandOffer offer) {
		if(getInventory().getItem(slot).getAmount() > 0) {
			totalStack--;
			totalMoney-=offer.getPrice() + Math.floor(offer.getPrice() * TAXE);
			if(totalStack <= 0) {
				amounts.remove(offer);
			} else {
				amounts.put(offer, getInventory().getItem(slot).getAmount());
			}
			indicator(slot, false);
			updateBuyItem();
		}
	}
	
	private void indicator(int slot, boolean increment) {
		ItemStack stack = getInventory().getItem(slot);
		if(increment)
			stack.setAmount(stack.getAmount()+1);
		else if(!increment && stack.getAmount() > 0)
			stack.setAmount(stack.getAmount()-1);
		
		if(stack.getAmount() > 0) {
			stack.setDurability((short) 13);
		} else {
			stack.setDurability((short) 14);
		}
		getInventory().setItem(slot, stack);
	}
	
	public void loadTops(Runnable callBack) {
		Requests.getTop(item, 4, true, new Callback<List<StandOffer>>() {
			
			@Override
			public void call(Throwable t, List<StandOffer> result) {
				if(t != null ) { t.printStackTrace(); return; } 
				
				if(result.size() == 0) { hasOffers = false; return; }
				
				for(int index = 0; index < result.size(); index++) {
					StandOffer offer = result.get(index);
					OfflinePlayer owner = Bukkit.getServer().getOfflinePlayer(offer.getOwner());
					ItemStack head = ItemUtils.createHead(offer.getOwner());
					ItemStack addToCart = ItemUtils.create("&aAjouter&7 ou&c retirer", new String[] {"", "&7> &bClic gauche&7 pour ajouter une unité", "&7> &bClic droit&7 pour retirer une unité"}, Material.STAINED_GLASS_PANE, 14);
					ItemStack concerned = ItemUtils.create("&7Vendeur: &b" + owner.getName(), new String[] {"&7Prix: &b" + offer.getPrice() + "$", "&7Taxe: &b" + Math.floor((offer.getPrice() * 0.10)) + "$"}, offer.getType(), offer.getAmount(), offer.getData());
					
					ClickableItem clickHead = new ClickableItem(head, new ItemAction() {
						
						@Override
						public void run(Player p, ItemStack clicked, int slot, InventoryAction action) {
							StandPlayer ownerPl = StandPlugin.get().getPlayer(offer.getOwner());
							ownerPl.openStand(p);
						}
					});
					
					ClickableItem clickAddToCart = new ClickableItem(addToCart, new ItemAction() {
						
						@Override
						public void run(Player p, ItemStack clicked, int slot, InventoryAction action) {	
							if(action == InventoryAction.PICKUP_HALF) { //Clic droit
								removeItemFromCart(slot, offer);
							} else {
								addItemToCart(slot, offer);
							}
						}
					});
					
					ClickableItem block = new ClickableItem(concerned, null);
					
					ClickableItem[][] menu = new ClickableItem[1][3];
					menu[0][0] = clickHead;
					menu[0][1] = clickAddToCart;
					menu[0][2] = block;
					
					MenuItem item = new MenuItem(menu);
					item.print(TopGUI.this, getPrintSlot(index, true));
				}
				
				second(callBack);
				
			}
		});
	}
	
	private void second(Runnable callBack) {
		Requests.getTop(item, 4, false, new Callback<List<StandOffer>>() {
			
			@Override
			public void call(Throwable t, List<StandOffer> result) {
				if(t != null ) { t.printStackTrace(); hasOffers = false; return; } 
				
				if(result.size() == 0) { hasOffers = false; return; }
				
				for(int index = 0; index < result.size(); index++) {
					StandOffer offer = result.get(index);
					OfflinePlayer owner = Bukkit.getServer().getOfflinePlayer(offer.getOwner());
					ItemStack head = ItemUtils.createHead(offer.getOwner());
					ItemStack addToCart = ItemUtils.create("&aAjouter&7 ou&c retirer", new String[] {"", "&7> &bClic gauche&7 pour ajouter une unité", "&7> &bClic droit&7 pour retirer une unité"}, Material.STAINED_GLASS_PANE, 14);
					ItemStack concerned = ItemUtils.create("&7Vendeur: &b" + owner.getName(), new String[] {"&7Prix: &b" + offer.getPrice() + "$", "&7Taxe: &b" + Math.floor((offer.getPrice() * 0.10)) + "$"}, offer.getType(), offer.getAmount(), offer.getData());
					
					ClickableItem clickHead = new ClickableItem(head, new ItemAction() {
						
						@Override
						public void run(Player p, ItemStack clicked, int slot, InventoryAction action) {
							StandPlayer ownerPl = StandPlugin.get().getPlayer(offer.getOwner());
							ownerPl.openStand(p);
						}
					});
					
					ClickableItem clickAddToCart = new ClickableItem(addToCart, new ItemAction() {
						
						@Override
						public void run(Player p, ItemStack clicked, int slot, InventoryAction action) {	
							if(action == InventoryAction.PICKUP_HALF) { //Clic droit
								removeItemFromCart(slot, offer);
							} else {
								addItemToCart(slot, offer);
							}
						}
					});
					
					ClickableItem block = new ClickableItem(concerned, null);
					
					ClickableItem[][] menu = new ClickableItem[1][3];
					menu[0][0] = clickHead;
					menu[0][1] = clickAddToCart;
					menu[0][2] = block;
					
					MenuItem item = new MenuItem(menu);
					item.print(TopGUI.this, getPrintSlot(index, true));
				}
				
				callBack.run();
			}
		});
	}
	
	private int getPrintSlot(int listIndex, boolean isTheCheaper) {
		int start = 1;
		if(!isTheCheaper) start = 5;
		return start + 8 * listIndex + listIndex;
	}
	
	@Override
	public boolean onClick(Player p, ItemStack clicked, ItemStack cursor, int slot, InventoryAction action,
			ClickType clickType, SlotType slotType) {
		return true;
	}

	@Override
	public void onClose(Player p) {
		
	}
	
	@Override
	public void show(Player p) {
		InventoryManager.getLoadingGui().show(p);
		loadTops(new Runnable() {
			@Override
			public void run() {
				if(hasOffers)
					TopGUI.super.show(p);
				else { 
					p.closeInventory();
					ChatUtils.sendMessage(p, "&cAucune offre n'a été trouvée pour cet item :c");
				}
			}
		});
	}
	
	public boolean give(PlayerInventory inv, ItemStack item){
		int maxStack = item.getMaxStackSize();
		int neededPlace = (item.getAmount() / maxStack) + (item.getAmount() % maxStack == 0 ? 0 : 1);
		int findedPlace = 0;

		for(int i=0;i<inv.getSize();i++){
			if(inv.getItem(i) == null){
				findedPlace++;
			}
		}

		int currentAmount = 0;
		int neededAmount = item.getAmount();

		if(findedPlace < neededPlace) return false;

		for(int i=0;i<inv.getSize();i++){
			if(currentAmount == neededAmount) break;
			if(inv.getItem(i) == null){
				int amount = maxStack;
				if(currentAmount + amount > neededAmount){
					amount = neededAmount - currentAmount;
				}

				currentAmount += amount;

				ItemStack is = item.clone(); is.setAmount(amount);
				inv.setItem(i, is);
			}
		}

		return true;
	}

	private boolean canGiveAll() {
		int totalNeededPlace = 0;
		int findedPlace = 0;
		
		for(StandOffer offer : amounts.keySet()) {
		
			ItemStack item = offer.getItem().clone();
			item.setAmount(amounts.get(offer));
			
			int maxStack = item.getMaxStackSize();
			int neededPlace = (item.getAmount() / maxStack) + (item.getAmount() % maxStack == 0 ? 0 : 1);
			
			totalNeededPlace+=neededPlace;

		}
		
		for(int slot = 0; slot < getPlayer().getInventory().getSize(); slot++) {
			if(getPlayer().getInventory().getItem(slot) == null) {
				findedPlace++;
			}
		}
		
		if(findedPlace < totalNeededPlace) return false;
		else return true;
	}
	
}
