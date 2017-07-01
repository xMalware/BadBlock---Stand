package com.lelann.stand.commands;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.lelann.stand.Requests;
import com.lelann.stand.objects.StandPlayer;
import com.lelann.stand.objects.StandRequest;

public class Buy extends AbstractCommand {

	public Buy() {
		super("buy", 
				"stand.play.buy", 
				"&c&l>&7 /stand buy &bitem quantit� prix unitaire", 
				"&7Cr�e une demande d'offre pour &bquantit� &7de l'&bitem&7 � &bprix unitaire&7 l'unit�", 
				"/stand buy <item> <quantit�> <prix>", 
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
			
			int emptySlots = 0;
			for(int i = 0; i < p.getInventory().getSize(); i++) {
				if(p.getInventory().getItem(i) == null) emptySlots++;
			}
			
			int amount = player.getWaiting().get(request);
			ItemStack concerned = request.createItemStack(amount);
			concerned.setAmount(amount);
			
			int needed = (amount / concerned.getMaxStackSize());
			
			if(emptySlots < needed) {
				sendMessage(sender, "&cVous n'avez pas assez de place dans votre inventaire.");
				return;
			}
			
			p.getInventory().addItem(concerned);
			sendMessage(sender, "&7Vous avez obtenu vos items !");
			
			player.getWaiting().remove(request);
			
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
			
			player.removeRequest(request);
			sendMessage(sender, "&7Demande supprim�e !");
			
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
				sendMessage(sender, "&cVous ne tenez pas d'item : /stand buy <item> <quantit�> <prix> dans ce cas !");
				return;
			}
			
			if(!validItem(sender, p.getItemInHand())) {
				return;
			}
			
			if(!validNumber(qtStr) || !validNumber(priceStr)) {
				sendMessage(sender, "&cVeuillez entrer un prix et une quantit� valides !");
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
				sendMessage(sender, "&cVous n'avez pas les fonds n�cessaires pour vendre " + amount + " de cet item !");
				return;
			}
			
			if(price <= 0) {
				sendMessage(sender, "&cMontant invalide");
				return;
			}
			
			ItemStack wanted = p.getItemInHand();
			wanted.setAmount(1);
			
			StandRequest newRequest = new StandRequest(player.getUniqueId(), wanted, price, amount);
			
			if(player.hasRequestedSame(newRequest)) {
				sendMessage(sender, "&cVous avez d�j� fait une offre pour cet item !");
				return;
			}
			
			player.addRequest(newRequest);
			
			Requests.saveRequest(newRequest);
			
			sendMessage(sender, "&7Une demande d'offre a bien �t� cr�e ! Vous pouvez la visualiser en tapant /stand buy list !");
			
		} else if(args.length == 3) { //PAS L'ITEM EN MAIN
			String itemStr = args[0];
			String qtStr = args[1];
			String priceStr = args[2];
			
			if(!validItemSpecified(sender, itemStr)) {
				return;
			}
			
			if(!validNumber(qtStr) || !validNumber(priceStr)) {
				sendMessage(sender, "&cVeuillez entrer un prix et une quantit� valides !");
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
				sendMessage(sender, "&cVous n'avez pas les fonds n�cessaires pour vendre " + amount + " de cet item !");
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
				sendMessage(sender, "&cVous avez d�j� fait une offre pour cet item !");
				return;
			}
			
			player.addRequest(newRequest);
			
			Requests.saveRequest(newRequest);
			
			sendMessage(sender, "&7Une demande d'offre a bien �t� cr�e ! Vous pouvez la visualiser en tapant /stand buy list !");
			
		} else {
			sendHelp(sender);
		}
		
	}
	
	private boolean validItemSpecified(CommandSender sender, String item) {
		return getItemSpecified(sender, item) != null;
	}
	
	private ItemStack getItemSpecified(CommandSender sender, String item) {
		// POSIBILIT�S: 
		// /stand buy stone
		// /stand buy stone:2
		// /stand buy 1
		// /stand buy 1:2
		ItemStack stack = null;
		if(item.contains(":")) {
			String idStr = item.split(":")[0];
			String dataStr = item.split(":")[0];
			
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
	
	private boolean validId(String id) {
		try {
			getMaterial(id);
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	
	@SuppressWarnings("deprecation")
	private Material getMaterial(String id) {
		id = id.toUpperCase();
		try {
			Material mat = null;
			if(validNumber(id)) {
				mat = Material.getMaterial(getNumber(id));
			} else {
				mat = Material.getMaterial(id);
			}
			if(mat == null) throw new RuntimeException();
			else return mat;
		} catch(RuntimeException e) {
			throw e;
		}
	}
	
	private boolean validItem(CommandSender sender, ItemStack item) {
		boolean valid = false;
		if(item.getDurability() != 0 && item.getType().getMaxDurability() != 0){
			sendMessage(sender, "&cVous ne pouvez pas ajouter un item abim� dans votre stand !");
		} else if(!item.getEnchantments().isEmpty()){
			sendMessage(sender, "&cLes items enchant�s sont interdits !");
		} else if(item.getType() == Material.ENCHANTED_BOOK
				|| item.getType() == Material.BOOK){
			sendMessage(sender, "&cLes livres sont interdits !");
		} else if(item.getType() == Material.FIREWORK){
			sendMessage(sender, "&cLes fus�es sont interdits !");
		} else if(item.getType() == Material.SKULL
				|| item.getType() == Material.SKULL_ITEM){
			sendMessage(sender, "&cLes t�tes sont interdites !");
		} else if(item.getType() == Material.BANNER){
			sendMessage(sender, "&cLes banni�res sont interdites !");
		} else {
			valid = true;
		}
		return valid;
	}
	
	private boolean validNumber(String number) {
		try {
			getNumber(number);
			return true;
		} catch(NumberFormatException e) {
			return false;
		}
	}
	
	private int getNumber(String number) {
		try {
			return Integer.parseInt(number);
		} catch(NumberFormatException e) {
			throw e;
		}
	}
	
}
