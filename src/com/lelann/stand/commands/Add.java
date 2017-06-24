package com.lelann.stand.commands;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.lelann.stand.Requests;
import com.lelann.stand.objects.StandOffer;
import com.lelann.stand.objects.StandPlayer;

public class Add extends AbstractCommand {
	public Add() {
		super("add", "stand.play.add", "%gold%/stand add %red%<prix unitaire>", "%gold%Ajoute l'item dans votre main à vos offres à %red%price %gold%l'unité.", "/stand add <type>", null);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void runCommand(CommandSender sender, String[] args) {
		if(!(sender instanceof Player)){
			sendMessage(sender, NO_CONSOLE);
			return;
		}

		PlayerInventory inv = ((Player) sender).getInventory();
		StandPlayer player = getPlayer(sender);

		if(args.length < 1){
			sendHelp(sender); return;
		}

		if(inv.getItemInHand() == null || inv.getItemInHand().getType() == Material.AIR){
			sendMessage(sender, "&cVous devez avoir un item dans la main pour utiliser cette commande !");  return;
		} else if(inv.getItemInHand().getDurability() != 0 && inv.getItemInHand().getType().getMaxDurability() != 0){
			sendMessage(sender, "&cVous ne pouvez pas ajouter un item abimé dans votre stand !");  return;
		} else if(!inv.getItemInHand().getEnchantments().isEmpty()){
			sendMessage(sender, "&cLes items enchantés sont interdits !");
		} else if(inv.getItemInHand().getType() == Material.ENCHANTED_BOOK
				|| inv.getItemInHand().getType() == Material.BOOK){
			sendMessage(sender, "&cLes livres sont interdits !");
		} else if(inv.getItemInHand().getType() == Material.FIREWORK){
			sendMessage(sender, "&cLes fusées sont interdits !");
		} else if(inv.getItemInHand().getType() == Material.SKULL
				|| inv.getItemInHand().getType() == Material.SKULL_ITEM){
			sendMessage(sender, "&cLes têtes sont interdites !");
		} else if(inv.getItemInHand().getType() == Material.BANNER){
			sendMessage(sender, "&cLes bannières sont interdites !");
		} else {
			StandOffer offer = player.getOffer(inv.getItemInHand());
			ItemStack item = inv.getItemInHand().clone();
			
			Material type = item.getType();
			byte data = item.getData().getData();

			if(offer == null && player.getMaxOfferNumber() <= player.getOffers().size()){
				sendMessage(sender, "&cVous avez atteind le nombre maximum d'offres possible, pensez à passer au grade supérieur !"); return;
			}

			int price = 0;
			try  {
				price = Integer.parseInt(args[0]);
				if(price < 0){
					throw new RuntimeException();
				}
			} catch(Exception e){
				sendMessage(sender, "&cLe prix indiqué n'est pas valide !"); return;
			}

			int count = 0;
			for(int i=0;i<inv.getSize();i++){
				ItemStack is = inv.getItem(i);
				if(is != null && is.getType() == type && is.getData().getData() == data){
					count += is.getAmount();
					inv.setItem(i, null);
				}
			}

			player.getPlayer().updateInventory();
			if(offer == null){
				offer = new StandOffer(player.getUniqueId(), item, price);
				offer.setAmount(0);
				player.getOffers().add(offer);
			}

			offer.add(count);
			offer.setPrice(price);

			Requests.saveOffer(offer);
			sendMessage(sender, "&eLes items ont bien été ajoutés !");
		}
	}
}