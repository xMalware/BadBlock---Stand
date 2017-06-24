package com.lelann.stand.listeners;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.lelann.stand.Requests;
import com.lelann.stand.abstracts.StandObject;
import com.lelann.stand.objects.StandOffer;
import com.lelann.stand.objects.StandPlayer;
import com.lelann.stand.objects.StandPlayer.StandAction;

public class InventoryListener extends StandObject implements Listener {
	public static final String INVENTORY_PNJ = "&c#PNJ";
	public static final String INVENTORY_TOP = "&cTop";

	@SuppressWarnings({ "unchecked", "deprecation" })
	@EventHandler
	public void onClick(InventoryClickEvent e){
		if(e.getCurrentItem() == null || e.getWhoClicked().getType() != EntityType.PLAYER)
			return;

		final StandPlayer player = getPlayer((Player) e.getWhoClicked());

		if(player == null) return;
		if(player.getAction() == StandAction.NOTHING
				|| player.getAction() == StandAction.A_LOOKING_PNJ
				|| player.getAction() == StandAction.A_WAITING_CHANGE_NAME
				|| player.getAction() == StandAction.A_WAITING_DEL_PNJ
				|| player.getAction() == StandAction.WAITING_KICK) return;
		if(e.getClickedInventory() instanceof PlayerInventory) {
			e.setCancelled(true);
			return;
		}

		e.setCancelled(true);
		
		if(player.getAction() == StandAction.LOOKING_TOP){
			List<StandOffer> offers = (List<StandOffer>) player.getActionCache();
			StandOffer offer = offers.get(e.getRawSlot());
			StandPlayer owner = getPlayer(offer.getOwner());

			if(e.getAction() == InventoryAction.PICKUP_HALF){ // clique droit
				owner.openStand((Player) e.getWhoClicked());

				player.setAction(StandAction.LOOKING_STAND);
				player.setActionCache(owner);
			} else { // clique gauche
				owner.buyOffer(player, offer);
			}
		} else if(player.getAction() == StandAction.LOOKING_STAND){
			StandPlayer owner = (StandPlayer) player.getActionCache();

			ItemStack item = e.getCurrentItem();
			Collection<StandOffer> offers = Collections.unmodifiableCollection(owner.getOffers());
			StandOffer offer = null;

			for(StandOffer off : offers){
				if(off.getType() == item.getType() && off.getData() == item.getData().getData()){
					offer = off; break;
				}
			}

			if(offer != null){
				if(e.getAction() == InventoryAction.PICKUP_HALF){ // clique droit
					if(player.getUniqueId().equals(owner.getUniqueId())
							|| player.getPlayer().hasPermission("stand.admin.modify")){
						if(!give(e.getWhoClicked().getInventory(), offer.createItemStack(offer.getAmount()))){
							player.sendMessage("&cVous n'avez pas assez de place dans l'inventaire pour enlever cette offre.");
						} else {
							player.sendMessage("&aL'offre a bien été supprimée !");
							offer.setAmount(0);
							owner.removeOffer(offer);
							e.getWhoClicked().closeInventory();
						}
					}
				} else { // clique gauche
					owner.buyOffer(player, offer);
				}
			} else {
				player.sendMessage("&cUne erreur c'est produite durant votre achat ...");
			}

		} else if(player.getAction() == StandAction.BUYING_OFFER) {
			StandOffer offer = (StandOffer) player.getActionCache();
			StandPlayer owner = getPlayer(offer.getOwner());

			int half = offer.getAmount() / 2;
			int amount = offer.getAmount();

			List<Integer> quant = StandPlayer.add(half, amount, 1, 8, 16, 32, 64);

			if(e.getSlot() == 8) {
				owner.openStand((Player) e.getWhoClicked());
			} else if(e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR){
				int slot = e.getSlot();
				if(slot < quant.size()){
					int q = quant.get(slot);
					if(q <= offer.getAmount()) {
						int price = offer.getPrice() * q;
						if(!player.hasEnough(price)){
							player.sendMessage("&cVous n'avez pas assez d'argent pour acheter cette offre ");
						} else {
							if(player.getUniqueId().equals(offer.getOwner())){
								player.sendMessage("&cVous ne pouvez pas acheter votre propre offre."); return;
							}

							if(!give(e.getWhoClicked().getInventory(), offer.createItemStack(q))){
								player.sendMessage("&cVous n'avez pas assez de place dans l'inventaire pour acheter cette offre.");
							} else {
								player.remove(price);
								owner.add(price);

								offer.remove(q);

								Requests.savePlayer(owner);

								if(offer.getAmount() <= 0){
									owner.removeOffer(offer);
								}

								player.getPlayer().closeInventory();
								owner.openStand((Player) e.getWhoClicked());

								if(owner.getPlayer() != null){
									owner.sendMessage("&a[Stand] Vous avez vendu " + q + " " + offer.getType().name().toLowerCase().replace("_", " ") + " à " + player.getPlayer().getDisplayName() + " &apour " + price + "$");
								}

								player.setAction(StandAction.LOOKING_STAND);
								player.setActionCache(owner);

								player.sendMessage("&aAchat réussi !");
							}
						}
					} else {
						player.sendMessage("&cUne erreur s'est produite pendant votre achat, réessayez.");
					}
				} else {
					player.sendMessage("&cUne nerreur s'est produite pendant votre achat, réessayez.");
				}
			}
		}
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
}
