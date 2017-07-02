package com.lelann.stand.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.lelann.factions.utils.ChatUtils;
import com.lelann.factions.utils.JRawMessage;
import com.lelann.factions.utils.JRawMessage.ClickEventType;
import com.lelann.factions.utils.JRawMessage.HoverEventType;
import com.lelann.stand.abstracts.StandObject;

import lombok.Getter;

public abstract class AbstractCommand extends StandObject {
	public static final String NO_PERM = "%red%Vous n'avez pas la permission d'executer cette commande !",
			NO_CONSOLE = "%red%Seul les joueurs peuvent utiliser cette commande !";
	public static final String PREFIX = "%darkaqua%[%aqua%Stand%darkaqua%] ";
//	private JRawMessage message;
	private JRawMessage[] messages;
	@Getter private String messageConsole, name, permission;

	public boolean hasPermission(CommandSender sender){
		return hasPermission(sender, permission);
	}
	
	public boolean hasPermission(CommandSender sender, String permission){
		if(permission.startsWith("stand.admin.") && sender.hasPermission("stand.admin.*")){
			return true;
		} else if(permission.startsWith("stand.play.") && sender.hasPermission("stand.play.*")){
			return true;
		} else {
			return sender.hasPermission("stand.*")
					|| sender.hasPermission("*")
					|| sender.hasPermission(permission);
		}
	}
	
	public void sendHelp(CommandSender sender) {
		if(messages != null && messages.length > 0) {
			if(sender instanceof Player){
				for(JRawMessage message : messages)
					message.send((Player) sender);
			} else ChatUtils.sendMessage(sender, messageConsole);
		}
	}
	
	protected void sendMessage(CommandSender sender, String msg){
		ChatUtils.sendMessage(sender, PREFIX + msg);
	}
	
	protected void broadcast(String msg){
		ChatUtils.broadcast(PREFIX + msg);
	}
	
	public AbstractCommand(String name, String permission) {
		this.name = name;
		this.permission = permission;
	}
	
	public AbstractCommand(String name, String permission, String description, String hover, String onClickShow, String onClickRun){
		messages = new JRawMessage[1];
		messages[0] = new JRawMessage(description);
		messageConsole = ChatUtils.colorReplace(description);
		this.permission = permission;
		this.name = name;
		
		messages[0].addHoverEvent(HoverEventType.SHOW_TEXT, hover);
		if(onClickShow != null)
			messages[0].addClickEvent(ClickEventType.SUGGEST_COMMAND, onClickShow, false);
		else if(onClickRun != null)
			messages[0].addClickEvent(ClickEventType.RUN_COMMAND, onClickRun, false);
	}
	
	public AbstractCommand(String name, String permission, String[] description, String[] hover, String[] onClickShow, String[] onClickRun){
		messages = new JRawMessage[description.length];
		for(int i = 0; i < messages.length; i++) {
			JRawMessage message = new JRawMessage(description[i]);
			if(hover != null && hover[i] != null)
				message.addHoverEvent(HoverEventType.SHOW_TEXT, hover[i]);
			if(onClickShow != null && onClickShow[i] != null)
				message.addClickEvent(ClickEventType.SUGGEST_COMMAND, onClickShow[i], false);
			else if(onClickRun != null && onClickRun[i] != null)
				message.addClickEvent(ClickEventType.RUN_COMMAND, onClickRun[i], false);
			messages[i] = message;
		}
		
		messageConsole = ChatUtils.colorReplace("&cAh nan :)");
		this.permission = permission;
		this.name = name;
	}
	
	public abstract void runCommand(CommandSender sender, String[] args);
}
