package com.lelann.stand.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.lelann.factions.utils.ChatUtils;
import com.lelann.factions.utils.StringUtils;
import com.lelann.stand.StandPlugin;
import com.lelann.stand.inventories.CategoryGUI;
import com.lelann.stand.inventories.abstracts.InventoryManager;
import com.lelann.stand.objects.CategoryPNJ;
import com.lelann.stand.objects.StandPlayer;

public class Pnj extends AbstractCommand {
	public Pnj() {
		super("pnj", "stand.admin.pnj", "%gold%/stand pnj create | setname | del", "%gold%Permet de gérer les PNJs", "/stand pnj", null);
	}

	@Override
	public void runCommand(CommandSender sender, String[] args) {
		if(!(sender instanceof Player)){
			sendMessage(sender, NO_CONSOLE);
			return;
		}

		Player player = (Player) sender;
		StandPlayer sPlayer = getPlayer(player);

		if(args.length == 0){
			sendHelp(sender); return;
		}
 
		if(args[0].equalsIgnoreCase("edit")) {
			
			String identifier = args[1];
			if(identifier == null) {
				sPlayer.sendMessage("&cVous devez spécifier une identifiant. Exemple: /stand pnj create blocs");
				return;
			}
			
			CategoryPNJ pnj = StandPlugin.get().getManager().getPnj(identifier);
			if(pnj == null) {
				sPlayer.sendMessage("&cL'identifiant n'est pas valide !");
				return;
			}
			CategoryGUI gui = InventoryManager.getCategoryGui(pnj);
			gui.openEdit(player);
			
		} else if(args[0].equalsIgnoreCase("create")){
			//StandTopPNJManager.getInstance().add(new StandTopPNJ(player.getLocation()));
			
			String identifier = args[1];
			if(identifier == null) {
				sPlayer.sendMessage("&cVous devez spécifier une identifiant. Exemple: /stand pnj create blocs");
				return;
			}
			
			StandPlugin.get().getManager().add(new CategoryPNJ(identifier, "Mon beau pnj", "Titre de l'inventaire", player.getLocation(), Arrays.asList(new ItemStack[9*5])));
			StandPlugin.get().getManager().reload();
			StandPlugin.get().getManager().savePnjs();
			
		} else if(args[0].equalsIgnoreCase("settitle")){
			//StandTopPNJManager.getInstance().add(new StandTopPNJ(player.getLocation()));
			
			String identifier = args[1];
			if(identifier == null) {
				sPlayer.sendMessage("&cVous devez spécifier une identifiant. Exemple: /stand pnj settitle blocs Un titre d'inventaire !");
				return;
			}
			
			if(args.length == 2){
				sPlayer.sendMessage("&cVeuillez préciser le nouveau nom du PNJ !");
			} else {
				String newName = StringUtils.join(args, " ", 2);
				newName = ChatUtils.colorReplace(newName);
				
				if(newName.length() > 32) {
					sPlayer.sendMessage("&cLe nouveau nom ne doit pas dépasser 32 caractères !");
					return;
				}

				CategoryPNJ pnj = StandPlugin.get().getManager().getPnj(identifier);
				if(pnj == null) {
					sPlayer.sendMessage("&cL'identifiant n'est pas valide !");
					return;
				}
				
				pnj.setGuiTitle(newName);
				
				StandPlugin.get().getManager().savePnjs();
			}
			
		} else if(args[0].equalsIgnoreCase("setname")){
			
			String identifier = args[1];
			if(identifier == null) {
				sPlayer.sendMessage("&cVous devez spécifier une identifiant. Exemple: /stand pnj setname blocs J'ai pleins de blocs !");
				return;
			}
			
			if(args.length == 2){
				sPlayer.sendMessage("&cVeuillez préciser le nouveau nom du PNJ !");
			} else {
				String newName = StringUtils.join(args, " ", 2);
				newName = ChatUtils.colorReplace(newName);
				
				if(newName.length() > 32) {
					sPlayer.sendMessage("&cLe nouveau nom ne doit pas dépasser 32 caractères !");
					return;
				}

				CategoryPNJ pnj = StandPlugin.get().getManager().getPnj(identifier);
				if(pnj == null) {
					sPlayer.sendMessage("&cL'identifiant n'est pas valide !");
					return;
				}
				
				pnj.getEntity().setCustomName(newName);
				pnj.setName(newName);
				
				StandPlugin.get().getManager().savePnjs();
			}
		} else if(args[0].equalsIgnoreCase("del")) {
			
			String identifier = args[1];
			if(identifier == null) {
				sPlayer.sendMessage("&cVous devez spécifier une identifiant. Exemple: /stand pnj del blocs");
				return;
			}
			
			CategoryPNJ pnj = StandPlugin.get().getManager().getPnj(identifier);
			if(pnj == null) {
				sPlayer.sendMessage("&cL'identifiant n'est pas valide !");
				return;
			}
			pnj.getEntity().remove();
			
			StandPlugin.get().getManager().getPnjs().remove(pnj.getEntity().getUniqueId());
			StandPlugin.get().getManager().savePnjs();
		}
	}
}