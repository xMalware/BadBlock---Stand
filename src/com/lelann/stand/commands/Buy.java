package com.lelann.stand.commands;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.lelann.stand.Requests;
import com.lelann.stand.objects.StandOffer;
import com.lelann.stand.objects.StandPlayer;
import com.lelann.stand.objects.StandRequest;

public class Buy extends AbstractCommand {

	public Buy() {
		super("buy", 
				"stand.play.buy", 
				"&c&l>&7 /stand buy &bitem quantité prix unitaire", 
				"&7Crée une demande d'offre pour &bquantité &7de l'&bitem&7 à &bprix unitaire&7 l'unité", 
				"/stand buy <item> <quantité> <prix>", 
				null);
	}

	@Override
	public void runCommand(CommandSender sender, String[] args) {
		if(!(sender instanceof Player)){
			sendMessage(sender, NO_CONSOLE);
			return;
		}

		Player p = (Player) sender;
		StandPlayer player = getPlayer(sender);

		if(args.length == 0) {
			sendHelp(sender);
			return;
		}
		
		String action = args[0];
		
		if(action.equalsIgnoreCase("getitems")) {
			if(args.length < 2) return;
			
			String idAndData = args[1];
			String id = idAndData.split(":")[0];
			String d = idAndData.split(":")[1];
			
			Material type = Material.valueOf(id);
			Byte data = Byte.parseByte(d);
			
			StandRequest request = player.getRequest(new ItemStack(type, 1, data));
			if(request == null) return;
			
			if(!player.getWaiting().containsKey(request) || player.getWaiting().get(request) <= 0)
				return;

			int amount = player.getWaiting().get(request);
			ItemStack concerned = request.createItemStack(amount);
			concerned.setAmount(amount);
			
			/* --- */
			
			int findedPlace = 0;

			for(int slot = 0; slot < p.getInventory().getSize(); slot++) {
				if(p.getInventory().getItem(slot) == null) {
					findedPlace++;
				}
			}
			
			int neededPlace = (amount / concerned.getMaxStackSize()) + (amount % concerned.getMaxStackSize() == 0 ? 0 : 1);
			
			if(findedPlace < neededPlace) {
				sendMessage(sender, "&cVous n'avez pas assez de place dans votre inventaire.");
				return;
			}
			
			/* --- */
			
			p.getInventory().addItem(concerned);
			sendMessage(sender, "&7Vous avez obtenu vos items !");
			
			player.getWaiting().remove(request);
			if(player.getCompleted().contains(request)) {
				player.deleteRequest(request);
			}
			
			
			return;
		}
		
		if(action.equalsIgnoreCase("removerequest")) {
			if(args.length < 2) return;
			
			String idAndData = args[1];
			String id = idAndData.split(":")[0];
			String d = idAndData.split(":")[1];
			
			Material type = Material.valueOf(id);
			Byte data = Byte.parseByte(d);
			
			StandRequest request = player.getRequest(new ItemStack(type, 1, data));
			if(request == null) return;
			
			player.deleteRequest(request);
			sendMessage(sender, "&7Demande supprimée !");
			
			return;
		}
		
		if(args.length == 1) {
			//list
			if(args[0].equalsIgnoreCase("list")) {
				player.listRequests();
			} else {
				sendHelp(sender);
			}
		} else if(args.length == 2) {
			String qtStr = args[0];
			String priceStr = args[1];
			
			if(p.getItemInHand() == null || p.getItemInHand().getType() == Material.AIR) {
				sendMessage(sender, "&cVous ne tenez pas d'item : /stand buy <item> <quantité> <prix> dans ce cas !");
				return;
			}
			
			if(!validItem(sender, p.getItemInHand())) {
				return;
			}
			
			if(!validNumber(qtStr) || !validNumber(priceStr)) {
				sendMessage(sender, "&cVeuillez entrer un prix et une quantité valides !");
				return;
			}
			
			int max = player.getMaxRequestNumber();
			if(max <= player.getRequests().size()) {
				sendMessage(sender, "&cVous avez atteind le nombre maximal de demandes.");
				return;
			}
			
			int amount = getNumber(qtStr);
			int price = getNumber(priceStr);
			int totalPrice = price * amount;
			
			if(totalPrice > player.getMoney()) {
				sendMessage(sender, "&cVous n'avez pas les fonds nécessaires pour demander " + amount + " de cet item !");
				return;
			}
			
			if(price <= 0) {
				sendMessage(sender, "&cMontant invalide");
				return;
			}
			
			if(price > MAX_PRICE) {
				sendMessage(sender, "&cMontant invalide. Montant maximal: " + MAX_PRICE);
				return;
			}
			
			ItemStack wanted = p.getItemInHand().clone();
			wanted.setAmount(1);
			
			StandRequest newRequest = new StandRequest(player.getUniqueId(), wanted, price, amount);
			
			if(player.hasRequestedSame(newRequest)) {
				sendMessage(sender, "&cVous avez déjà fait une demande pour cet item !");
				return;
			}
			
			player.addRequest(newRequest);
			
			Requests.saveRequest(newRequest);
			
			sendMessage(sender, "&7Une demande d'offre a bien été crée ! Vous pouvez la visualiser en tapant /stand buy list !");
			
		} else if(args.length == 3) { //PAS L'ITEM EN MAIN
			String itemStr = args[0];
			String qtStr = args[1];
			String priceStr = args[2];
			
			if(!validItemSpecified(sender, itemStr)) {
				return;
			}
			
			if(!validNumber(qtStr) || !validNumber(priceStr)) {
				sendMessage(sender, "&cVeuillez entrer un prix et une quantité valides !");
				return;
			}
			
			int max = player.getMaxRequestNumber();
			if(max <= player.getRequests().size()) {
				sendMessage(sender, "&cVous avez atteind le nombre maximal de demandes.");
				return;
			}
			
			int amount = getNumber(qtStr);
			int price = getNumber(priceStr);
			int totalPrice = price * amount;
			
			if(totalPrice > player.getMoney()) {
				sendMessage(sender, "&cVous n'avez pas les fonds nécessaires pour demander " + amount + " de cet item !");
				return;
			}
			
			if(price <= 0) {
				sendMessage(sender, "&cMontant invalide");
				return;
			}
			
			ItemStack wanted = getItemSpecified(sender, itemStr);
			wanted.setAmount(1);
			
			StandRequest newRequest = new StandRequest(player.getUniqueId(), wanted, price, amount);
			
			if(player.hasRequestedSame(newRequest)) {
				sendMessage(sender, "&cVous avez déjà fait une demande pour cet item !");
				return;
			}
			
			player.addRequest(newRequest);
			
			Requests.saveRequest(newRequest);
			
			sendMessage(sender, "&7Une demande d'offre a bien été crée ! Vous pouvez la visualiser en tapant /stand buy list !");
			
		} else {
			sendHelp(sender);
		}
		
	}
	
	private boolean validItemSpecified(CommandSender sender, String item) {
		return getItemSpecified(sender, item) != null;
	}
	
	private ItemStack getItemSpecified(CommandSender sender, String item) {
		ItemStack stack = null;
		if(item.contains(":")) {
			String idStr = item.split(":")[0];
			String dataStr = item.split(":")[1];
			
			if(!validNumber(dataStr)) {
				sendMessage(sender, "&cItem invalide (" + idStr + ":&l" + dataStr + "&c)");
				return null;
			}
			
			if(!validId(idStr)) {
				sendMessage(sender, "&cItem invalide (&l" + idStr + "&c:" + dataStr + "&c)");
				return null;
			}
			
			Material mat = getMaterial(idStr);
			byte data = (byte) getNumber(dataStr);
			
			stack = new ItemStack(mat, 1, data);
		} else {
			if(!validId(item)) {
				sendMessage(sender, "&cItem invalide (&l" + item + "&c)");
				return null;
			}
			
			stack = new ItemStack(getMaterial(item));
		}
		
		return stack;
	}
	
	public boolean validItem(CommandSender sender, ItemStack item) {
		boolean valid = false;
		if(item.getDurability() != 0 && item.getType().getMaxDurability() != 0){
			sendMessage(sender, "&cVous ne pouvez pas ajouter un item abimé dans votre stand !");
		} else if(!item.getEnchantments().isEmpty()){
			sendMessage(sender, "&cLes items enchantés sont interdits !");
		} else if(item.getType() == Material.ENCHANTED_BOOK
				|| item.getType() == Material.BOOK){
			sendMessage(sender, "&cLes livres sont interdits !");
		} else if(item.getType() == Material.FIREWORK){
			sendMessage(sender, "&cLes fusées sont interdits !");
		} else if(item.getType() == Material.SKULL
				|| item.getType() == Material.SKULL_ITEM){
			sendMessage(sender, "&cLes têtes sont interdites !");
		} else if(item.getType() == Material.BANNER){
			sendMessage(sender, "&cLes bannières sont interdites !");
		} else {
			valid = true;
		}
		return valid;
	}
	
}
