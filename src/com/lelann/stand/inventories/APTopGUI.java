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
import org.bukkit.inventory.meta.ItemMeta;

import com.lelann.factions.Main;
import com.lelann.factions.api.Faction;
import com.lelann.factions.api.FactionChunk;
import com.lelann.factions.database.Callback;
import com.lelann.factions.utils.ChatUtils;
import com.lelann.factions.utils.ItemUtils;
import com.lelann.factions.utils.StringUtils;
import com.lelann.stand.Requests;
import com.lelann.stand.StandPlugin;
import com.lelann.stand.inventories.abstracts.AbstractInventory;
import com.lelann.stand.inventories.abstracts.ClickableItem;
import com.lelann.stand.inventories.abstracts.InventoryManager;
import com.lelann.stand.inventories.items.MenuItem;
import com.lelann.stand.objects.APOffer;
import com.lelann.stand.objects.APRequest;
import com.lelann.stand.objects.StandFaction;
import com.lelann.stand.objects.StandPlayer;
import com.lelann.stand.selection.MathsUtils;

public class APTopGUI extends AbstractInventory {

	private List<APOffer> topOffers = new ArrayList<>();
	private List<APRequest> topRequests = new ArrayList<>();
	
	private Map<APOffer, Integer> amounts = new HashMap<>();
	private Map<Integer, APOffer> offersBySlots = new HashMap<>();
	
	private Map<APRequest, Integer> amountsRequests = new HashMap<>();
	private Map<Integer, APRequest> requestsBySlots = new HashMap<>();
	
	private Map<APRequest, List<FactionChunk>> apToGive = new HashMap<>();
	private List<FactionChunk> alreadySelected = new ArrayList<>();
	
	private int totalBuy, totalSell, totalMoneyBuy, totalMoneySell = 0;
	private final double TAXE = 0.10;
	
	private boolean hasOffers = true, hasRequests = true;
	private Faction fac;
	private StandFaction faction;
	private StandPlayer player;
	
	public APTopGUI(Player viewer) {
		super("&7Voir les ventes et le demandes", viewer);
		fac = Main.getInstance().getPlayersManager().getPlayer(viewer).getFaction();
		faction = StandPlugin.get().getStandFaction(fac);
		player = StandPlugin.get().getPlayer(viewer);
		setup();
	}

	public double taxe(int price) {
		return MathsUtils.round(price * TAXE, 2);
	}
	
	public void setup() {
		InventoryManager.addGui(this);
		setBottomBar(false, true);
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
		
		ClickableItem aps = new ClickableItem(ItemUtils.create("&7Vos APs", new String[] {"&7Accédez à vos APs"}, Material.OBSIDIAN), new ItemAction() {
			@Override
			public void run(Player p, ItemStack clicked, int slot, InventoryAction action) {
				faction.openGui(player);
			}
		});
		
		editBottomBar(4, book);
		editBottomBar(0, aps);
	}
	
	private void sellSelectedItems() {
		if(amountsRequests.isEmpty()) {
			sendFMessage("&cVous devez sélectionner au moins un item avant de continuer.");
			return;
		}
		
		for(APRequest selling : amountsRequests.keySet()) {
			StandFaction current = StandPlugin.get().getStandFaction(selling.getOwner());
			Faction other = selling.getOwner();
			int gived = amountsRequests.get(selling);
			List<FactionChunk> apsGived = apToGive.get(selling);
			
			if(current.getFaction().getCapital() < (selling.getWantedPrice() * gived)) {
				if(gived > 0) {
					fac.sendMessage("&eVous n'avez pas pu vendre les APs &c"
							+ StringUtils.join(apsGived, " &f+&c ") + "&e à la faction &c" + selling.getOwner().getName() + "&e (Capital trop faible !)");
				} else {
					fac.sendMessage("&eVous n'avez pas pu vendre l'AP &c" + apsGived.get(0).toString() + "&e (Capital trop faible !)");
				}
				continue;
			}
			
			List<String> aps = new ArrayList<>();
			
			for(int i = 0; i < apsGived.size(); i++) {
				FactionChunk toGive = apsGived.get(i);
				
				Main.getInstance().getChunksManager(toGive.getWorld()).setOnSale(toGive, false);
				toGive.getAllowedMembers().clear();
				toGive.setFactionId(other.getFactionId());

				other.setChunkNumber(other.getChunkNumber() + 1);
				other.setApChunkNumber(other.getApChunkNumber() + 1);
				fac.setApChunkNumber(fac.getApChunkNumber() - 1);
				fac.setChunkNumber(fac.getChunkNumber() - 1);
				
				other.updateScoreboard();
				fac.updateScoreboard();
				aps.add("&c- &eAP en &c" + toGive.toString());
				
				StandPlugin.get().getProtector().unprotect(toGive);
				toGive.save(false); 
			}
			
			fac.addToCapital((selling.getWantedPrice() * gived) - (int) taxe(selling.getWantedPrice() * gived));
			other.removeFromCapital(selling.getWantedPrice() * gived);
			
			fac.sendMessage("&eVous venez de vendre:");
			fac.sendMessage(aps.toArray(new String[aps.size()]));
			fac.sendMessage("&ePour la somme de &c" + ((selling.getWantedPrice() * gived) - (int) taxe(selling.getWantedPrice() * gived)) + "$&e (&c" + (selling.getWantedPrice() - (int) taxe(selling.getWantedPrice())) + "$&e chacun) &cTAXE: 10% (&c" + taxe(selling.getWantedPrice() * gived) + "$&e en tout)");
			
			other.sendMessage("&eVous venez d'acheter:");
			other.sendMessage(aps.toArray(new String[aps.size()]));
			other.sendMessage("&ePour la somme de &c" + (selling.getWantedPrice() * gived) + "$&e chacun)");
			
			selling.remove(gived);
			if(selling.getWantedAmount() <= 0) {
				current.removeRequest(selling);
			}
			
			other.save(false);
			fac.save(false); 
			
			current.save();
			faction.save();
			
			//c.save(false); other.save(false); fac.save(false); faction.save();
		}
		
		regenerate();
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
			FactionChunk c = buying.getAp();
			
			Main.getInstance().getChunksManager(c.getWorld()).setOnSale(c, false);
			c.getAllowedMembers().clear();
			c.setFactionId(fac.getFactionId());
			c.setLastVisited(System.currentTimeMillis());
			
			fac.setApChunkNumber(fac.getApChunkNumber() + 1);
			fac.setChunkNumber(fac.getChunkNumber() + 1);			
			fac.removeFromCapital(buying.getPrice() + (int) taxe(buying.getPrice()));
			fac.updateScoreboard();
			fac.sendMessage(PREFIX_FACTION + "&eVous avez acheté l'AP de &c" + other.getName() + "&e en &c" + c.toString() + "&e pour &c" + (buying.getPrice() + (int) taxe(buying.getPrice())) + "$&e !");

			if(!other.equals(Faction.BADBLOCK)) {
				other.addToCapital(buying.getPrice());
				other.setChunkNumber(other.getChunkNumber() - 1);
				other.setApChunkNumber(other.getApChunkNumber() - 1);
				other.updateScoreboard();
				other.sendMessage(PREFIX_FACTION + "&eVous avez vendu votre AP en &c" + c.toString() + "&e à &c" + fac.getName() + "&e pour &c" + (buying.getPrice()) + "$&e !");
				other.save(false);
			}
			
			StandPlugin.get().getProtector().unprotect(c);
			faction.removeOffer(buying);
			
			c.save(false); fac.save(false); faction.save();
		}
		
		regenerate();
	}
	
	private void addUnit(boolean isOffer, int slot) {
		
		if(isOffer) {
		
			if(totalSell > 0) {
				totalSell = 0;
				resetRequests();
			}
			
			APOffer offer = offersBySlots.get(slot);
			
			if(offer == null) {
				return;
			}
			
			if(getInventory().getItem(slot).getAmount() < 1 || getInventory().getItem(slot).getDurability() == 14) {
				totalBuy++;
				totalMoneyBuy += offer.getPrice() + taxe(offer.getPrice());
				indicator(slot, true);
				amounts.put(offer, amounts.get(offer) == null ? getInventory().getItem(slot).getAmount() : amounts.get(offer) + 1);
				updateBuyItem(isOffer);
			} else {
				System.out.println("wtf == " + (getInventory().getName() + " --- " + getInventory().getTitle() + " --- " + getPlayer().getName()));
				System.out.println("wtf == " + (getInventory().getItem(slot).getAmount() + " A, ") + (getInventory().getItem(slot).getDurability() + " D"));
			}
			
		} else {
			if(totalBuy > 0) {
				totalBuy = 0;
				resetOffers();
			}
			
			APRequest req = requestsBySlots.get(slot);
			
			if(req == null) {
				return;
			}
			
			if(getInventory().getItem(slot).getAmount() < req.getWantedAmount() || getInventory().getItem(slot).getDurability() == 14) {
				
				if(requestsBySlots.get(slot).getOwner().getFactionId() == faction.getFaction().getFactionId()) return;
				
				APGui gui = new APGui(player, faction, true);
				gui.openSelect(this, alreadySelected.isEmpty() ? apToGive.get(req) : alreadySelected, new Callback<FactionChunk>() {

					@Override
					public void call(Throwable t, FactionChunk result) {
						
						if(apToGive.get(req) == null) apToGive.put(req, new ArrayList<>());
						
						List<FactionChunk> toGive = apToGive.get(req);
						toGive.add(result);
						apToGive.put(req, toGive);
						updateCurrentItem(slot);
						
						alreadySelected.add(result);
						
						totalSell++;
						totalMoneySell += req.getWantedPrice() - taxe(req.getWantedPrice());
						indicator(slot, true);
						amountsRequests.put(req, amountsRequests.get(req) == null ? getInventory().getItem(slot).getAmount() : amountsRequests.get(req) + 1);
						updateBuyItem(isOffer);
					}
					
				});
			}
		}
		
		update();
	}
	
	private void updateCurrentItem(int slot) {
		int cSlot = slot + 1;
		//Req
		APRequest req = requestsBySlots.get(slot);
		//Concerned
		ItemStack st = getInventory().getItem(cSlot);
		ItemMeta meta = st.getItemMeta();
		List<String> lore = meta.getLore().subList(0, 3);
		if(apToGive.get(req) != null && apToGive.get(req).size() > 0) {
			lore.add("");
			lore.add(ChatUtils.colorReplace("&7Vous vendez les APs:"));
			List<FactionChunk> toGive = apToGive.get(req);
			for(int pos = 0; pos < toGive.size(); pos++) {
				FactionChunk c = toGive.get(pos);
				lore.add(ChatUtils.colorReplace("&b" + (pos+1) + ") &7AP en &b" + c.toString()));
			}
		}
		meta.setLore(lore);
		st.setItemMeta(meta);
		getInventory().setItem(cSlot, st);
	}
	
	private void removeUnit(boolean isOffer, int slot) {
		
		if(isOffer) {
		
			APOffer offer = offersBySlots.get(slot);
			
			if(offer == null) {
				return;
			}
			
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
			
			APRequest req = requestsBySlots.get(slot);
			
			if(req == null) {
				return;
			}
			
			if(getInventory().getItem(slot).getAmount() > 1 || getInventory().getItem(slot).getDurability() == 13 && totalSell > 0) {
				
				if(requestsBySlots.get(slot).getOwner().getFactionId() == faction.getFaction().getFactionId()) return;
				
				List<FactionChunk> toGive = apToGive.get(req);
				FactionChunk toRem = toGive.get(toGive.size()-1);
				toGive.remove(toGive.size()-1);
				apToGive.put(req, toGive);
				updateCurrentItem(slot);
				
				alreadySelected.remove(toRem);
				
				totalSell--;
				totalMoneySell -= req.getWantedPrice();
				indicator(slot, false);
				amountsRequests.put(req, amountsRequests.get(req) == null ? getInventory().getItem(slot).getAmount() : amountsRequests.get(req) - 1);
				
				if(amountsRequests.get(req) <= 0) {
					amountsRequests.remove(req);
				}
				
				updateBuyItem(isOffer);
			}
			
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
				newStack = ItemUtils.create("&aValider votre achat", new String[] {"&7Vous êtes sur le point de vendre&b " + totalSell + "&7 de vos APs",
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
					//run(cb);
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
		
		topRequests = null;
		Requests.getAPRequests(4, new Callback<List<APRequest>>() {
			@Override
			public void call(Throwable t, List<APRequest> result) {
				if(t != null || result == null || result.size() == 0) {
					hasRequests = false;
					topRequests = new ArrayList<>();
					//run(cb);
					return;
				}
				hasRequests = true;
				topRequests = result;
			}
		});
		
		if(sync) {
			while(topRequests == null) {
				try {
					Thread.sleep(3L);
				} catch (InterruptedException e) { }
			}
		}
		
		printAll();
		run(cb);
	}
	
	private void printAll() {
		printOffers();
		printRequests();
	}
	
	private void printRequests() {
		for(int index = 0; index < topRequests.size(); index++) {
			APRequest offer = topRequests.get(index);
			ItemStack head = ItemUtils.createHead("&7Faction: &6" + offer.getOwner().getName(), offer.getOwner().getLeader().getLastUsername());
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
						removeUnit(false, slot);
					} else {
						addUnit(false, slot);
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
			
			requestsBySlots.put(slot+1, offer);
		}
	}
	
	private void printOffers() {
		for(int index = 0; index < topOffers.size(); index++) {
			APOffer offer = topOffers.get(index);
			ItemStack head = null; 
			
			if(offer.getOwner().equals(Faction.BADBLOCK) || offer.getOwner().getLeader() == null) {
				head = ItemUtils.create("&7Faction: &6" + offer.getOwner().getName(), Material.DIAMOND_BLOCK);
			} else {
				head = ItemUtils.createHead("&7Faction: &6" + offer.getFactionName(), offer.getOwner().getLeader().getLastUsername());
			}
			
			ItemStack addToCart = ItemUtils.create("&aAjouter&7 ou&c retirer", new String[] {"", "&7> &bClic gauche&7 pour ajouter une unité", "&7> &bClic droit&7 pour retirer une unité"}, Material.STAINED_GLASS_PANE, 14);
			ItemStack concerned = offer.createItemStack();
			
			ItemAction headAction = new ItemAction() {
				@Override
				public void run(Player p, ItemStack clicked, int slot, InventoryAction action) {
					if(!offer.getOwner().equals(Faction.BADBLOCK)) {
						offer.getOwner().sendInfos(p);
					} else {
						sendFMessage("&cPas d'information pour cette faction.");
					}
				}
			};
			ItemAction addAction = new ItemAction() {
				@Override
				public void run(Player p, ItemStack clicked, int slot, InventoryAction action) {
					if(action == InventoryAction.PICKUP_HALF) {
						removeUnit(true, slot);
					} else {
						addUnit(true, slot);
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
			int slot = getPrintSlot(index, true);
			item.print(APTopGUI.this, slot);
			
			offersBySlots.put(slot+1, offer);
		}
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
				
				if(hasOffers || hasRequests) {
					if(back != null) {
						back.displayGui(APTopGUI.this);
					} else {
						show();
					}
				} else { 
					goBack();
					ChatUtils.sendMessage(p, "&cAucune offre ni demande n'ont été trouvées pour cet item :c");
					return;
				}
			}
		}, true);
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
			amountsRequests.remove(requestsBySlots.get(slot));
			ItemStack base = getInventory().getItem(slot);
			base.setAmount(1);
			base.setDurability((short) 14);
			getInventory().setItem(slot, base);
		}
		updateBuyItem(true);
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
		new APTopGUI(getPlayer()).showBefore();
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
