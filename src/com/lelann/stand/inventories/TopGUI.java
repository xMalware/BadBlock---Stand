package com.lelann.stand.inventories;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.lelann.factions.Main;
import com.lelann.factions.api.FactionPlayer;
import com.lelann.factions.database.Callback;
import com.lelann.factions.utils.ChatUtils;
import com.lelann.factions.utils.ItemUtils;
import com.lelann.factions.utils.JRawMessage;
import com.lelann.factions.utils.JRawMessage.ClickEventType;
import com.lelann.stand.Requests;
import com.lelann.stand.StandPlugin;
import com.lelann.stand.inventories.abstracts.AbstractInventory;
import com.lelann.stand.inventories.abstracts.ClickableItem;
import com.lelann.stand.inventories.abstracts.InventoryManager;
import com.lelann.stand.inventories.items.MenuItem;
import com.lelann.stand.objects.StandOffer;
import com.lelann.stand.objects.StandPlayer;
import com.lelann.stand.objects.StandRequest;
import com.lelann.stand.selection.MathsUtils;

public class TopGUI extends AbstractInventory {

	private ItemStack item;
	private boolean hasOffers = true;
	private boolean hasRequests = true;
	
	private final double TAXE = 0.10;
	
	private double totalMoney = 0;
	private int totalStack = 0;
	
	private double totalMoneySell = 0;
	private int totalSell = 0;

	private Map<StandOffer, Integer> amounts = new HashMap<>();
	private Map<Integer, StandOffer> offersBySlots = new HashMap<>();
	
	private Map<StandRequest, Integer> requestAmounts = new HashMap<>();
	private Map<Integer, StandRequest> requestsBySlots = new HashMap<>();
	
	public TopGUI(String title, ItemStack item, Player viewer) {
		super(title, viewer);
		this.item = item;
		setup();
	}
	
	public double taxe(int price) {
		return MathsUtils.round(((price * TAXE)) * 100, 2) / 100.0;
	}

	public void setup() {
		InventoryManager.addGui(this);
		setBottomBar(true, true);
		ClickableItem book = new ClickableItem(ItemUtils.create("&aValider votre achat", new String[] {"&cVous devez au moins avoir un item dans votre panier", "&cpour finaliser votre achat !"}, Material.BOOK), new ItemAction() {
			
			@Override
			public void run(Player p, ItemStack clicked, int slot, InventoryAction action) {
				
				if(totalStack > 0) {
					buySelectedItems();
				} else if(totalSell > 0) {
					sellSelectedItems();
				} else {
					ChatUtils.sendMessage(p, "&cVous n'avez pas séléctionné d'items !");
				}
			}
		});
		editBottomBar(4, book);
	}
	
	public void updateBuyItem(ItemStack newItem) {
		editBottomBar(4, getBarClickable(4).update(newItem));
	}
	
	public void updateBuyItem(String name, String[] description) {
		editBottomBar(4, getBarClickable(4).update(ItemUtils.create(getInventory().getItem(4), name, description)));
	}
	
	public void updateBuyItem(ClickableItem item) {
		editBottomBar(4, item);
	}
	
	public void updateBuyItem(boolean isOffer) {
		StandPlayer player = StandPlugin.get().getPlayer(getPlayer());
		ItemStack newStack = null;
		
		if(isOffer) {
			if(totalStack > 0) {
				newStack = ItemUtils.create("&aValider votre achat", new String[] {"&7Vous êtes sur le point d'acheter&b " + totalStack + "&7 items",
					"&7Argent: &b" + player.getMoney() + "$",
					"&7Coût: &b" + totalMoney + "$",
					"&7Après achat: &b" + (player.getMoney()-totalMoney) + "$",
					"",
					"&7Cliquez pour acheter !"}, Material.BOOK);
			} else {
				newStack = ItemUtils.create("&aValider", new String[] {"&cCommencez par choisir si vous voulez vendre à un acheteur", "&cou acheter à un vendeur :)"}, Material.BOOK);
			}
		} else {
			if(totalSell > 0) {
				newStack = ItemUtils.create("&aValider votre achat", new String[] {"&7Vous êtes sur le point de vendre&b " + totalSell + "&7 items",
						"&7Argent: &b" + player.getMoney() + "$",
						"&7Gain: &b+" + totalMoneySell + "$",
						"&7Après achat: &b" + (player.getMoney()+totalMoneySell) + "$",
						"",
						"&7Cliquez pour vendre !"}, Material.BOOK);
			} else {
				newStack = ItemUtils.create("&aValider", new String[] {"&cCommencez par choisir si vous voulez vendre à un acheteur", "&cou acheter à un vendeur :)"}, Material.BOOK);
			}
		}
		getInventory().setItem(getSize() - 9 + 4, newStack);
		update();
	}
	
	private void sellSelectedItems() {
		if(requestAmounts.isEmpty()) {
			ChatUtils.sendMessage(getPlayer(), "&cVous devez séléctionner au moins un item à vendre !");
			return;
		}
		
		StandPlayer player = StandPlugin.get().getPlayer(getPlayer());
		
		if(!canSellAll()) {
			player.sendMessage("&cVous n'avez pas les items nécessaires !");
			return;
		}
		
		for(StandRequest selling : requestAmounts.keySet()) {
			if(requestAmounts.get(selling) <= 0) continue;
			
			if(selling.getOwner().equals(getPlayer().getUniqueId())) {
				player.sendMessage("&cT'es un marrant toi, en fait.");
				continue;
			}
			
			StandPlayer owner = selling.getPlayer(selling.getOwner());
			FactionPlayer p = Main.getInstance().getPlayersManager().getPlayer(selling.getOwner());
			
			int amount = requestAmounts.get(selling);
			long priceForOwner = (long) (selling.getWantedPrice() * amount);
			int priceForPlayer = amount * selling.getWantedPrice();
			
			//removing money and adding items
			player.add(priceForPlayer);
			owner.remove(priceForOwner);
			
			selling.remove(amount);
			
			if(selling.getWantedAmount() <= 0){
				owner.removeRequest(selling);
			}
			
			JRawMessage msg = new JRawMessage(PREFIX + "Vous avez acheté &a" + amount + " " + selling.getName() + "&7 à &a" + player.getPlayer().getName() + "&7 pour &a" + priceForOwner + "$&7 ! Cliquez pour voir vos demandes et récupérer vos objets !");
			msg.addClickEvent(ClickEventType.RUN_COMMAND, "/stand buy list", false);
			
			if(owner.getPlayer() != null){
				//owner.sendMessage(PREFIX + "Vous venez d'acheter &a" + amount + " " + selling.getName() + "&7 à &a" + player.getPlayer().getName() + "&7 pour &a" + priceForOwner + "$&7 !");
				msg.send(owner.getPlayer());
			} else {
				owner.addRequestMsg(msg);
			}
			
			owner.updateRequest(selling);
			
			ItemStack item = selling.getItem();
			item.setAmount(amount);
			
			owner.getWaiting().put(selling, owner.getWaiting().get(selling) == null ? amount : owner.getWaiting().get(selling) + amount);
			removeItems(item, getPlayer().getInventory(), amount);
			
			player.sendMessage(PREFIX + "Vous venez de vendre &a" + amount + " " + selling.getName() + "&7 à &a" + p.getLastUsername() + "&7 pour &a" + priceForPlayer + "$&7 !");
			
			Requests.savePlayer(player);
			Requests.savePlayer(owner);
			
		}
		
		regenerate();
		
	}
	
	private void buySelectedItems() {
		if(amounts.isEmpty()) {
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
			
			if(amounts.get(buying) <= 0) continue;
			
			if(buying.getOwner().equals(getPlayer().getUniqueId())) {
				player.sendMessage("&cT'es un marrant toi, en fait.");
				continue;
			}
			
			StandPlayer owner = buying.getPlayer(buying.getOwner());
			FactionPlayer p = Main.getInstance().getPlayersManager().getPlayer(buying.getOwner());
			
			int amount = amounts.get(buying);
			long priceForPlayer = (long) (buying.getPrice() * amount + taxe(amount * buying.getPrice()));
			int priceForOwner = amount * buying.getPrice();
			
			//removing money and adding items
			player.remove(priceForPlayer);
			owner.add(priceForOwner);
			
			buying.remove(amount);
			
			if(buying.getAmount() <= 0){
				owner.removeOffer(buying);
			}
			
			if(owner.getPlayer() != null){
				owner.sendMessage(PREFIX + "Vous venez de vendre &a" + amount + " " + buying.getName() + "&7 à &a" + player.getPlayer().getName() + "&7 pour &a" + priceForOwner + "$&7 !");
			}
			
			ItemStack item = buying.getItem();
			item.setAmount(amount);
			
			player.getPlayer().getInventory().addItem(item);
			player.sendMessage(PREFIX + "Vous venez d'acheter &a" + amount + " " + buying.getName() + "&7 à &a" + p.getLastUsername() + "&7 pour &a" + priceForPlayer + "$&7 !");
			
			Requests.savePlayer(player);
			Requests.savePlayer(owner);
		}
		
		regenerate();
		
	}
	
	private void resetOffers() {
		for(int slot : offersBySlots.keySet()) {
			amounts.remove(offersBySlots.get(slot));
			ItemStack base = getInventory().getItem(slot);
			base.setAmount(1);
			base.setDurability((short) 14);
			getInventory().setItem(slot, base);
		}
		updateBuyItem(true);
	}
	
	private void resetRequests() {
		for(int slot : requestsBySlots.keySet()) {
			requestAmounts.remove(requestsBySlots.get(slot));
			ItemStack base = getInventory().getItem(slot);
			base.setAmount(1);
			base.setDurability((short) 14);
			getInventory().setItem(slot, base);
		}
		updateBuyItem(true);
	}
	
	private void addItemToCart(boolean isOffer, int slot) {
		
		if(isOffer) {
		
			if(totalSell > 0) {
				totalSell = 0;
				resetRequests();
			}
			
			StandOffer offer = offersBySlots.get(slot);
			
			if(offer == null) {
				return;
			}
			
			if(getInventory().getItem(slot).getAmount() < offer.getAmount() || getInventory().getItem(slot).getDurability() == 14) {
				totalStack++;
				totalMoney += offer.getPrice() + taxe(offer.getPrice());
				indicator(slot, true);
				amounts.put(offer, amounts.get(offer) == null ? getInventory().getItem(slot).getAmount() : amounts.get(offer) + 1);
				updateBuyItem(isOffer);
			}
			
		} else {
			if(totalStack > 0) {
				totalStack = 0;
				resetOffers();
			}
			
			StandRequest request = requestsBySlots.get(slot);
			
			if(request == null) {
				return;
			}
			
			if(getInventory().getItem(slot).getAmount() < request.getWantedAmount() || getInventory().getItem(slot).getDurability() == 14) {
				totalSell++;
				totalMoneySell += request.getWantedPrice();
				indicator(slot, true);
				requestAmounts.put(request, requestAmounts.get(request) == null ? getInventory().getItem(slot).getAmount() : requestAmounts.get(request) + 1);
				updateBuyItem(isOffer);
			}
			
		}
		
		update();
	}
	
	private void removeItemFromCart(boolean isOffer, int slot) {
		
		if(isOffer) {
		
			StandOffer offer = offersBySlots.get(slot);
			
			if(getInventory().getItem(slot).getAmount() > 1 || getInventory().getItem(slot).getDurability() == 13 && totalStack > 0) {
				totalStack--;
				totalMoney-=offer.getPrice() + taxe(offer.getPrice());
				indicator(slot, false);
				amounts.put(offer, amounts.get(offer) == null ? getInventory().getItem(slot).getAmount() : amounts.get(offer) - 1);
				
				if(amounts.get(offer) <= 0) {
					amounts.remove(offer);
				}
				
				updateBuyItem(isOffer);
			}
			
		} else {
			
			StandRequest request = requestsBySlots.get(slot);
			
			if(getInventory().getItem(slot).getAmount() > 1 || getInventory().getItem(slot).getDurability() == 13 && totalSell > 0) {
				totalSell--;
				totalMoneySell-=request.getWantedPrice();
				indicator(slot, false);
				requestAmounts.put(request, requestAmounts.get(request) == null ? getInventory().getItem(slot).getAmount() : requestAmounts.get(request) - 1);
				
				if(requestAmounts.get(request) <= 0) {
					requestAmounts.remove(request);
				}
				
				updateBuyItem(isOffer);
			}
			
		}
		
		update();
	}
	
	private void indicator(int slot, boolean increment) {
		ItemStack stack = getInventory().getItem(slot);
		
		if(increment) {
			if(stack.getDurability() == 13) {
				stack.setAmount(stack.getAmount()+1);
			} else {
				stack.setDurability((short) 13);
			}
		} else if(!increment && stack.getAmount() > 1) {
			stack.setAmount(stack.getAmount()-1);
		} else {
			if(!increment && stack.getDurability() == 13) {
				stack.setDurability((short) 14);
			}
		}
		getInventory().setItem(slot, stack);
		update();
	}
	
	private boolean found = false;
	
	public void loadTops(Runnable callBack, Runnable cbTimeout, int timeout) {
		
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(StandPlugin.get(), new Runnable() {
			
			@Override
			public void run() {
				if(!found) {
					cbTimeout.run();
				}
				
			}
		}, timeout * 20L);
		
		Requests.getTop(item, 4, true, new Callback<List<StandOffer>>() {
			
			@Override
			public void call(Throwable t, List<StandOffer> result) {
				if(t != null || result == null || result.size() == 0) { 
					hasOffers = false; 
					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(StandPlugin.get(), callBack, 1L); 
					return;
				}
				
				int printIndex = 0;
				
				for(int index = 0; index < result.size(); index++) {
					StandOffer offer = result.get(index);
					FactionPlayer owner = Main.getInstance().getPlayersManager().getPlayer(offer.getOwner());
					
					String name = "???";
					
					if(owner != null)
						name = owner.getLastUsername();
					else {
						if(printIndex > 0)
						printIndex--;
						continue;
					}
					
					ItemStack head = ItemUtils.createHead("&7Stand: " + StandPlugin.get().getPlayer(offer.getOwner()).getStandName(), name);
					ItemStack addToCart = ItemUtils.create("&aAjouter&7 ou&c retirer", new String[] {"", "&7> &bClic gauche&7 pour ajouter une unité", "&7> &bClic droit&7 pour retirer une unité"}, Material.STAINED_GLASS_PANE, 14);
					ItemStack concerned = ItemUtils.create("&7Vendeur: &b" + name, new String[] {"&7Prix: &b" + offer.getPrice() + "$", "&7Taxe: &b" + taxe(offer.getPrice()) + "$"}, offer.getType(), offer.getAmount(), offer.getData());
					
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
								removeItemFromCart(true, slot);
							} else {
								addItemToCart(true, slot);
							}
						}
					});
					
					ClickableItem block = new ClickableItem(concerned, null);
					
					ClickableItem[][] menu = new ClickableItem[1][3];
					menu[0][0] = clickHead;
					menu[0][1] = clickAddToCart;
					menu[0][2] = block;
					
					MenuItem item = new MenuItem(menu);
					int slot = getPrintSlot(printIndex, true);
					item.print(TopGUI.this, slot);
					
					printIndex++;
					
					offersBySlots.put(slot+1, offer);
				}
				
				second(callBack);
				
			}
		});
	}
	
	private void second(Runnable callBack) {
		Requests.getTopRequests(item, 4, new Callback<List<StandRequest>>() {
			@Override
			public void call(Throwable t, List<StandRequest> requests) {
				if(t != null || requests == null || requests.size() == 0) {
					hasRequests = false;
					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(StandPlugin.get(), callBack, 1L);
					return;
				}
				
				int printIndex = 0;
				for(int index = 0; index < requests.size(); index++) {
					if(!printRequest(printIndex, requests.get(index))) {
						if(printIndex > 0)
							printIndex--;
					} else {
						printIndex++;
					}
				}
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(StandPlugin.get(), callBack, 1L);
				
			}
		});
	}
	
	private boolean printRequest(int printIndex, StandRequest request) {
		FactionPlayer owner = Main.getInstance().getPlayersManager().getPlayer(request.getOwner());
		
		String name = "???";
		
		if(owner != null)
			name = owner.getLastUsername();
		else {
			return false;
		}
		
		ItemStack head = ItemUtils.createHead("&7Stand: " + StandPlugin.get().getPlayer(request.getOwner()).getStandName(), name);
		ItemStack addToCart = ItemUtils.create("&aAjouter&7 ou&c retirer", new String[] {"", "&7> &bClic gauche&7 pour ajouter une unité", "&7> &bClic droit&7 pour retirer une unité"}, Material.STAINED_GLASS_PANE, 14);
		ItemStack concerned = ItemUtils.create("&7Acheteur: &b" + name, new String[] {"&7Prix voulu: &b" + request.getWantedPrice() + "$"}, request.getType(), request.getWantedAmount(), request.getData());
		
		ClickableItem clickHead = new ClickableItem(head, new ItemAction() {
			
			@Override
			public void run(Player p, ItemStack clicked, int slot, InventoryAction action) {
				StandPlayer ownerPl = StandPlugin.get().getPlayer(request.getOwner());
				ownerPl.openStand(p);
			}
		});
		
		ClickableItem clickAddToCart = new ClickableItem(addToCart, new ItemAction() {
			
			@Override
			public void run(Player p, ItemStack clicked, int slot, InventoryAction action) {	
				if(action == InventoryAction.PICKUP_HALF) { //Clic droit
					removeItemFromCart(false, slot);
				} else {
					addItemToCart(false, slot);
				}
			}
		});
		
		ClickableItem block = new ClickableItem(concerned, null);
		
		ClickableItem[][] menu = new ClickableItem[1][3];
		menu[0][0] = clickHead;
		menu[0][1] = clickAddToCart;
		menu[0][2] = block;
		
		MenuItem item = new MenuItem(menu);
		int slot = getPrintSlot(printIndex, false);
		item.print(TopGUI.this, slot);
		
		requestsBySlots.put(slot+1, request);
		
		return true;
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
		//InventoryManager.removeGui(this);
	}
	
	public void showBefore() {
		Player p = getPlayer();
		AbstractInventory back = InventoryManager.getGui(getPlayer().getOpenInventory().getTopInventory(), getPlayer());
		InventoryManager.getLoadingGui(p).show();
		loadTops(new Runnable() {
			@Override
			public void run() {
				
				found = true;
				
				if(!hasOffers && !hasRequests) {
					goBack();
					ChatUtils.sendMessage(p, "&cAucune offre ni demande n'ont été trouvées pour cet item :c");
					return;
				}
				
				if(hasOffers || hasRequests) {
					if(back != null) {
						back.displayGui(TopGUI.this);
					} else {
						show();
					}
				} else { 
					goBack();
					ChatUtils.sendMessage(p, "&cAucune offre ni demande n'ont été trouvées pour cet item :c");
				}
			}
		}, new Runnable() {
			
			@Override
			public void run() {
				ChatUtils.sendMessage(p, "&cLa requête a pris trop de temps.");
				p.closeInventory();
				
			}
		}, 10);
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
	
	private boolean canSellAll() {
		int totalStackNeeded = 0;
		for(int amounts : requestAmounts.values()) {
			totalStackNeeded += amounts;
		}
		
		return hasAmount(item, getPlayer().getInventory(), totalStackNeeded);
		
	}
	
	public boolean hasAmount(ItemStack mat, Inventory inv, int amt) {
		int invamt = 0;
		for (ItemStack i : inv) {
			if(i != null && i.getType() == mat.getType() && i.getDurability() == mat.getDurability()){
				invamt = invamt + i.getAmount();
			}
		}
		if(invamt >= amt){
			return true;
		} else {
			return false;
		}
	}
	
	public void removeItems(ItemStack item, Inventory inv, int amount) {
		int count = amount;
		for(int i=0;i<inv.getSize();i++){
			ItemStack is = inv.getItem(i);
			if(is != null && is.getType() == item.getType() && is.getData().getData() == item.getData().getData()){
				if(count > 0) {
					if(is.getAmount() > amount) {
						is.setAmount(is.getAmount()-amount);
						count = 0;
					} else {
						count -= is.getAmount();
						is = null;
					}
					inv.setItem(i, is);
				}
			}
		}
	}
	
	public void regenerate() {
		
		hasOffers = true;
		hasRequests = true;
		
		totalMoney = 0;
		totalStack = 0;
		
		totalMoneySell = 0;
		totalSell = 0;

		amounts = new HashMap<>();
		offersBySlots = new HashMap<>();
		
		requestAmounts = new HashMap<>();
		requestsBySlots = new HashMap<>();
		
		goBack();
		InventoryManager.restore(this);
		showBefore();
	}
	
}
