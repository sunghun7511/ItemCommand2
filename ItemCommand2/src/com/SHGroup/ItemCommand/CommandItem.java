package com.SHGroup.ItemCommand;

import org.bukkit.inventory.ItemStack;

public class CommandItem{
	public ItemStack item;
	public String command;
	public int time;
	public CommandItem(ItemStack item, String command, int time){
		this.item = item;
		this.command = command;
		this.time = time;
	}
	
}
