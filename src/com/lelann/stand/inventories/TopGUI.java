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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.lelann.factions.Main;
import com.lelann.factions.api.FactionPlayer;
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
	
	private double totalMoney = 0;
	private int totalStack = 0;

	private Map<StandOffer, Integer> amounts = new HashMap<>();
	private Map<Integer, StandOffer> offersBySlots = new HashMap<>();
	
	public TopGUI(String title, ItemStack item) {
		super(title);
		this.item = item;
		setup();
	}
	
	public double taxe(int price) {
		return Math.round(((price * TAXE)) * 100) / 100.0;
	}

	public void setup() {
		InventoryManager.addGui(this);
		setBottomBar(true, true);
		ClickableItem book = new ClickableItem(ItemUtils.create("&aValider votre achat", new String[] {"&cVous devez au moins avoir un item dans votre panier", "&cpour finaliser votre achat !"}, Material.BOOK), new ItemAction() {
			
			@Override
			public void run(Player p, ItemStack clicked, int slot, InventoryAction action) {
				
				System.out.println("OMG, CLICKED BOOK");
				
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
		editBottomBar(4, getBarClickable(4).update(newItem));
	}
	
	public void updateBuyItem(String name, String[] description) {
		editBottomBar(4, getBarClickable(4).update(ItemUtils.create(getInventory().getItem(4), name, description)));
	}
	
	public void updateBuyItem(ClickableItem item) {
		editBottomBar(4, item);
	}
	
	public void updateBuyItem() {
		StandPlayer player = StandPlugin.get().getPlayer(getPlayer());
		ItemStack newStack = null;
		
		if(totalStack > 0) {
			newStack = ItemUtils.create("&aValider votre achat", new String[] {"&7Vous êtes sur le point d'acheter&b " + totalStack + "&7 items",
				"&7Argent: &b" + player.getMoney() + "$",
				"&7Coût: &b" + totalMoney + "$",
				"&7Après achat: &b" + (player.getMoney()-totalMoney) + "$",
				"",
				"&7Cliquez pour acheter !"}, Material.BOOK);
		} else {
			newStack = ItemUtils.create("&aValider votre achat", new String[] {"&cVous devez au moins avoir un item dans votre panier", "&cpour finaliser votre achat !"}, Material.BOOK);
		}
		//editBottomBar(4, getBarClickable(4).update(newStack));
		getInventory().setItem(getSize() - 9 + 4, newStack);
		update();
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
			
			if(amounts.get(buying) < 0) continue;
			
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
			player.sendMessage(PREFIX + "&8&l[&7Stand&8&l]&7 Vous venez d'acheter &a" + amount + " " + buying.getName() + "&7 à &a" + p.getLastUsername() + "&7 pour &a" + priceForPlayer + "$&7 !");
			
			Requests.savePlayer(owner);
			//getPlayer().closeInventory();
		}
		
		getPlayer().closeInventory();
		
	}
	
	private void addItemToCart(int slot) {
		
		StandOffer offer = offersBySlots.get(slot);
		
		if(offer == null) {
			System.out.println("OFFER IS NULL RETURNING");
			return;
		}
		System.out.println("current amount : " + getInventory().getItem(slot).getAmount() + " slot=" + slot + ", type=" + offer.getItem().getType() + ", offer: " + offer.getAmount() + ", owner: " + offer.getOwner());
		if(getInventory().getItem(slot).getAmount() < offer.getAmount() || getInventory().getItem(slot).getDurability() == 14) {
			totalStack++;
			totalMoney += offer.getPrice() + taxe(offer.getPrice());
			indicator(slot, true);
			amounts.put(offer, getInventory().getItem(slot).getAmount());
			updateBuyItem();
			System.out.println("it's ok");
		} else {
			System.out.println("cannot add to cart !");
		}
		update();
	}
	
	private void removeItemFromCart(int slot) {
		
		StandOffer offer = offersBySlots.get(slot);
		
		if(getInventory().getItem(slot).getAmount() > 1 || getInventory().getItem(slot).getDurability() == 13 && totalStack > 0) {
			totalStack--;
			totalMoney-=offer.getPrice() + taxe(offer.getPrice());
			indicator(slot, false);
			if(totalStack <= 0) {
				amounts.remove(offer);
			} else {
				amounts.put(offer, getInventory().getItem(slot).getAmount());
			}
			updateBuyItem();
		} else {
			System.out.println("cannot remove :x");
		}
		update();
	}
	
	private void indicator(int slot, boolean increment) {
		System.out.println("updating item at slot " + slot + " !");
		ItemStack stack = getInventory().getItem(slot);
		System.out.println("before: amount: " + stack.getAmount() + ", data: " + stack.getDurability());
		
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
		
		/*if(stack.getAmount() > 1) {
			stack.setDurability((short) 13);
		} else {
			stack.setDurability((short) 14);
		}*/
		System.out.println("after: amount: " + stack.getAmount() + ", data: " + stack.getDurability());
		//getInventory().setItem(slot, null);
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
				if(t != null ) { t.printStackTrace(); return; } 
				
				if(result.size() == 0) { hasOffers = false; callBack.run(); return; }
				
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
							System.out.println("CLCIKED ADD/REMOVE TO CART !!!");
							if(action == InventoryAction.PICKUP_HALF) { //Clic droit
								removeItemFromCart(slot);
							} else {
								addItemToCart(slot);
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
		Requests.getTop(item, 4, false, new Callback<List<StandOffer>>() {
			
			@Override
			public void call(Throwable t, List<StandOffer> result) {
				if(t != null || result == null) { t.printStackTrace(); hasOffers = false; return; } 
				
				if(result.size() == 0) { hasOffers = false; callBack.run(); return; }
				
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
								removeItemFromCart(slot);
							} else {
								addItemToCart(slot);
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
					
					printIndex++;
					
					offersBySlots.put(slot+1, offer);
				}
				
				System.out.println("calling cb ! Finished loading tops !");
				
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(StandPlugin.get(), callBack, 1L);
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
		System.out.println("Clicked !, item: " + clicked + ",,, slot=" + slot);
		return true;
	}

	@Override
	public void onClose(Player p) {
		InventoryManager.removeGui(this);
	}
	
	public void showBefore(Player p) {
		setPlayer(p);
		AbstractInventory before = InventoryManager.getGui(p.getOpenInventory().getTopInventory());
		InventoryManager.getLoadingGui().show(p);
		loadTops(new Runnable() {
			@Override
			public void run() {
				
				found = true;
				
				if(hasOffers) {
					if(before != null) {
						before.displayGui(TopGUI.this);
					} else {
						show(p);
					}
				} else { 
					p.closeInventory();
					ChatUtils.sendMessage(p, "&cAucune offre n'a été trouvée pour cet item :c");
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
