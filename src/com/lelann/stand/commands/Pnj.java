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
import com.lelann.stand.objects.ApPNJ;
import com.lelann.stand.objects.CategoryPNJ;
import com.lelann.stand.objects.StandPlayer;

public class Pnj extends AbstractCommand {
	public Pnj() {
		super("pnj", 
		"stand.admin.pnj", 
		new String[] {
				"&8&m----------------------------",
				"&c&l>&7 /stand pnj create &bidentifier",
				"&c&l>&7 /stand pnj edit &bidentifier",
				"&c&l>&7 /stand pnj setname &bidentifier name",
				"&c&l>&7 /stand pnj settitle &bidentifier title",
				"&c&l>&7 /stand pnj del &bidentifier",
				"&c&l>&7 /stand pnj list"
				}, 
		new String[] {
				null,
				"&7Crée un pnj identifié avec &bidentifier&7 sur votre position",
				"&7Ouvre le menu du pnj &bidentifier&7 en mode édition, pour une modification plus facile et plus rapide",
				"&7Définit le nouveau nom du pnj &bidentifier&7 pour &bname",
				"&7Définit le titre du menu du pnj &bidentifier&7 pour &btitle",
				"&7Supprime le pnj &bidentifier",
				"&7Liste les pnjs existants"
				}, 
		new String[] {
				null,
				"/stand pnj create ",
				"/stand pnj edit ",
				"/stand pnj setname ",
				"/stand pnj settitle ",
				"/stand pnj del ",
				"/stand pnj list"
				}, 
		null);
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
			CategoryGUI gui = InventoryManager.getCategoryGui(player, pnj);
			gui.openEdit(player);
			
		} else if(args[0].equalsIgnoreCase("create")){
			//StandTopPNJManager.getInstance().add(new StandTopPNJ(player.getLocation()));
			
			String identifier = args[1];
			if(identifier == null) {
				sPlayer.sendMessage("&cVous devez spécifier une identifiant. Exemple: /stand pnj create blocs");
				return;
			}
			
			if(identifier.equalsIgnoreCase("ap" )) {
				StandPlugin.get().getManager().add(new ApPNJ("APs", "Titre de l'inventaire", player.getLocation(), 2));
				StandPlugin.get().getManager().reload();
				StandPlugin.get().getManager().savePnjs();
				return;
			}
			
			StandPlugin.get().getManager().add(new CategoryPNJ(identifier, "Mon beau pnj", "Titre de l'inventaire", player.getLocation(), Arrays.asList(new ItemStack[9*5]), 1));
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
				
				StandPlugin.get().getManager().reload();
				StandPlugin.get().getManager().savePnjs();
				
				sPlayer.sendMessage("&aLe titre de l'inventaire a été modifié !");
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
			pnj.delete();
		} else if(args[0].equalsIgnoreCase("open")) {
			
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
			
			pnj.openGui(player);
			
		} else if(args[0].equalsIgnoreCase("list")) {
			StandPlugin.get().getManager().sendList(player);
		}
	}
}