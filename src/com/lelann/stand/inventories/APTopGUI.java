package com.lelann.stand.inventories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.ItemStack;

import com.lelann.factions.Main;
import com.lelann.factions.api.Faction;
import com.lelann.factions.api.FactionChunk;
import com.lelann.factions.api.managers.ChunksManager;
import com.lelann.factions.database.Callback;
import com.lelann.factions.utils.ChatUtils;
import com.lelann.factions.utils.ItemUtils;
import com.lelann.stand.Requests;
import com.lelann.stand.StandPlugin;
import com.lelann.stand.inventories.abstracts.AbstractInventory;
import com.lelann.stand.inventories.abstracts.ClickableItem;
import com.lelann.stand.inventories.abstracts.InventoryManager;
import com.lelann.stand.inventories.items.MenuItem;
import com.lelann.stand.objects.APOffer;
import com.lelann.stand.objects.StandFaction;

public class APTopGUI extends AbstractInventory {

	private List<APOffer> topOffers = new ArrayList<>();
	//private List<APRequest> topRequests = new ArrayList<>();
	
	private Map<APOffer, Integer> amounts = new HashMap<>();
	private Map<Integer, APOffer> offersBySlots = new HashMap<>();
	
	private int totalBuy, totalSell, totalMoneyBuy, totalMoneySell = 0;
	
	private boolean hasOffers = true, hasRequests = true, found = false;
	private Faction fac;
	
	public APTopGUI(Player viewer) {
		super("&7Voir les ventes et le demandes", viewer);
		fac = Main.getInstance().getPlayersManager().getPlayer(viewer).getFaction();
		setup();
	}

	public void setup() {
		InventoryManager.addGui(this);
		setBottomBar(true, true);
		ClickableItem book = new ClickableItem(ItemUtils.create("&aValider votre achat", new String[] {"&cVous devez au moins avoir un item dans votre panier", "&cpour finaliser votre achat !"}, Material.BOOK), new ItemAction() {
			
			@Override
			public void run(Player p, ItemStack clicked, int slot, InventoryAction action) {
				
				if(totalBuy > 0) {
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
	
	private void buySelectedItems() {
		if(amounts.isEmpty()) {
			sendFMessage("&cVous devez sélectionner au moins un item avant de continuer.");
			return;
		}
		
		if(fac.getApChunkNumber() >= 4) {
			sendFMessage("&cVous avez atteint le nombre maximal d'APs que vous pouvez acheter.");
			regenerate();
			return;
		}
		
		if(fac.getApChunkNumber() + totalBuy > 4) {
			sendFMessage("&cVous allez dépasser la limite d'APs en faisant ceci.");
			regenerate();
			return;
		}
		
		if(!canBuyAll()) {
			sendFMessage("&cVous n'avez pas assez de capital pour tout acheter !");
			regenerate();
			return;
		}
		
		for(APOffer buying : amounts.keySet()) {
			
			if(amounts.get(buying) <= 0) continue;
			
			if(buying.getOwner().getFactionId() == fac.getFactionId()) {
				sendFMessage("&cT'es un marrant toi, en fait.");
				continue;
			}
			
			Faction other = buying.getOwner();
			
			ChunksManager cm = Main.getInstance().getChunksManager(getPlayer().getLocation().getWorld());
			FactionChunk c = cm.getFactionChunk(getPlayer().getLocation().getChunk());
			
			c.getAllowedMembers().clear();
			c.setFactionId(fac.getFactionId());

			other.setChunkNumber(other.getChunkNumber() - 1);
			other.setApChunkNumber(other.getApChunkNumber() - 1);
			fac.setApChunkNumber(fac.getApChunkNumber() + 1);
			fac.setChunkNumber(fac.getChunkNumber() + 1);

			other.updateScoreboard();
			fac.updateScoreboard();
			
			other.sendMessage(PREFIX + "%red%" + fac.getName() + "%yellow% a bien reçu votre AP !");
			fac.sendMessage(PREFIX + "%yellow%Vous avez bien reçu l'AP de %red%" + other.getName() + "%yellow% !");

			c.save(false); other.save(false); fac.save(false);
		}
	}
	
	private void sellSelectedItems() {
		
	}
	
	private void addUnit(boolean isOffer, int slot) {
		
		if(isOffer) {
		
			/*if(totalSell > 0) {
				totalSell = 0;
				resetRequests();
			} TODO */
			
			APOffer offer = offersBySlots.get(slot);
			
			if(offer == null) {
				return;
			}
			
			if(getInventory().getItem(slot).getAmount() < 1 || getInventory().getItem(slot).getDurability() == 14) {
				totalBuy++;
				totalMoneyBuy += offer.getPrice();
				indicator(slot, true);
				amounts.put(offer, amounts.get(offer) == null ? getInventory().getItem(slot).getAmount() : amounts.get(offer) + 1);
				updateBuyItem(isOffer);
			}
			
		} else {
			//todo
			
		}
		
		update();
	}
	
	private void removeUnit(boolean isOffer, int slot) {
		
		if(isOffer) {
		
			APOffer offer = offersBySlots.get(slot);
			
			if(getInventory().getItem(slot).getAmount() > 1 || getInventory().getItem(slot).getDurability() == 13 && totalBuy > 0) {
				totalBuy--;
				totalMoneyBuy -= offer.getPrice();
				indicator(slot, false);
				amounts.put(offer, amounts.get(offer) == null ? getInventory().getItem(slot).getAmount() : amounts.get(offer) - 1);
				
				if(amounts.get(offer) <= 0) {
					amounts.remove(offer);
				}
				
				updateBuyItem(isOffer);
			}
			
		} else {
			
			//TODO
			
		}
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
	
	public boolean canBuyAll() {
		int totalMoney = 0;
		for(APOffer offer : amounts.keySet()) {
			totalMoney += offer.getPrice() * amounts.get(offer);
		}
		return totalMoney <= fac.getCapital();
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
		//StandPlayer player = StandPlugin.get().getPlayer(getPlayer());
		StandFaction faction = StandPlugin.get().getStandFaction(fac);
		ItemStack newStack = null;
		
		if(isOffer) {
			if(totalBuy > 0) {
				newStack = ItemUtils.create("&aValider votre achat", new String[] {"&7Vous êtes sur le point d'acheter&b " + totalBuy + "&7 APs",
					"&7Capital: &b" + faction.getFaction().getCapital() + "$",
					"&7Coût: &b" + totalMoneyBuy + "$",
					"&7Après achat: &b" + (faction.getFaction().getCapital()-totalMoneyBuy) + "$",
					"",
					"&7Cliquez pour acheter !"}, Material.BOOK);
			} else {
				newStack = ItemUtils.create("&aValider", new String[] {"&cCommencez par choisir si vous voulez vendre à un acheteur", "&cou acheter à un vendeur :)"}, Material.BOOK);
			}
		} else {
			if(totalSell > 0) {
				newStack = ItemUtils.create("&aValider votre achat", new String[] {"&7Vous êtes sur le point de vendre&b " + totalBuy + "&7 de vos APs",
						"&7Capital: &b" + faction.getFaction().getCapital() + "$",
						"&7Gain: &b+" + totalMoneySell + "$",
						"&7Après achat: &b" + (faction.getFaction().getCapital()+totalMoneySell) + "$",
						"",
						"&7Cliquez pour acheter !"}, Material.BOOK);
			} else {
				newStack = ItemUtils.create("&aValider", new String[] {"&cCommencez par choisir si vous voulez vendre à un acheteur", "&cou acheter à un vendeur :)"}, Material.BOOK);
			}
		}
		getInventory().setItem(getSize() - 9 + 4, newStack);
		update();
	}

	public void loadTops(Runnable cb, boolean sync) {
		topOffers = null;
		Requests.getAPsOnSale(4, new Callback<List<APOffer>>() {
			@Override
			public void call(Throwable t, List<APOffer> result) {
				if(t != null || result == null || result.size() == 0) {
					hasOffers = false;
					topOffers = new ArrayList<>();
					run(cb);
					return;
				}
				hasOffers = true;
				topOffers = result;
			}
		});
		
		if(sync) {
			while(topOffers == null) {
				try {
					Thread.sleep(3L);
				} catch (InterruptedException e) { }
			}
		}
		
		loadRequests(cb, true);
	}
	
	private void loadRequests(Runnable cb, boolean sync) {
		//TODO
		printAll();
		hasRequests = false;
		run(cb);
	}
	
	private void printAll() {
		//PRINTING OFFERS
		for(int index = 0; index < topOffers.size(); index++) {
			APOffer offer = topOffers.get(index);
			ItemStack head = ItemUtils.createHead("&7Faction: &6" + offer.getFactionName(), offer.getOwner().getLeader().getLastUsername());
			ItemStack addToCart = ItemUtils.create("&aAjouter&7 ou&c retirer", new String[] {"", "&7> &bClic gauche&7 pour ajouter une unité", "&7> &bClic droit&7 pour retirer une unité"}, Material.STAINED_GLASS_PANE, 14);
			ItemStack concerned = offer.createItemStack();
			
			ItemAction headAction = new ItemAction() {
				@Override
				public void run(Player p, ItemStack clicked, int slot, InventoryAction action) {
					offer.getOwner().sendInfos(p);
				}
			};
			ItemAction addAction = new ItemAction() {
				@Override
				public void run(Player p, ItemStack clicked, int slot, InventoryAction action) {
					if(action == InventoryAction.PICKUP_HALF) {
						addUnit(true, slot);
					} else {
						removeUnit(true, slot);
					}
				}
			};
			
			ClickableItem clickHead = new ClickableItem(head, headAction);
			ClickableItem clickAddToCart = new ClickableItem(addToCart, addAction);
			ClickableItem block = new ClickableItem(concerned, null);
			
			ClickableItem[][] menu = new ClickableItem[1][3];
			menu[0][0] = clickHead;
			menu[0][1] = clickAddToCart;
			menu[0][2] = block;
			
			MenuItem item = new MenuItem(menu);
			int slot = getPrintSlot(index, false);
			item.print(APTopGUI.this, slot);
			
			offersBySlots.put(slot+1, offer);
		}
		//TODO: PRINTING REQUESTS
	}
	
	private int getPrintSlot(int listIndex, boolean isTheCheaper) {
		int start = 1;
		if(!isTheCheaper) start = 5;
		return start + 8 * listIndex + listIndex;
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
						back.displayGui(APTopGUI.this);
					} else {
						show();
					}
				} else { 
					goBack();
					ChatUtils.sendMessage(p, "&cAucune offre ni demande n'ont été trouvées pour cet item :c");
				}
			}
		}, true);
	}
	
	public void regenerate() {
		
		hasOffers = true;
		hasRequests = true;
		
		totalMoneyBuy = 0;
		totalBuy = 0;
		
		totalMoneySell = 0;
		totalSell = 0;

		amounts = new HashMap<>();
		offersBySlots = new HashMap<>();
		
		//requestAmounts = new HashMap<>();
		//requestsBySlots = new HashMap<>();
		
		goBack();
		InventoryManager.restore(this);
		showBefore();
	}
	
	/* -------- UNUSED --------- */
	
	@Override
	public boolean onClick(Player p, ItemStack clicked, ItemStack cursor, int slot, InventoryAction action,
			ClickType clickType, SlotType slotType) {
		return true;
	}

	@Override
	public void onClose(Player p) {	}
	
}
