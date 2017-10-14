package com.SHGroup.ItemCommand;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	public ArrayList<CommandItem> list = new ArrayList<CommandItem>();
	public String pr;

	@Override
	public void onEnable() {
		this.getServer().getPluginManager().registerEvents(this, this);
		try {
			File f = new File("plugins/ItemCommand/servername.txt");
			if (!f.exists()) {
				new File("plugins/ItemCommand/").mkdirs();
				f.createNewFile();
				BufferedWriter bw = new BufferedWriter(new FileWriter(f));
				bw.append("&7&l[&b&llCommandItem&7&l] &e&l");
				bw.flush();
				bw.close();
			}
			BufferedReader br = new BufferedReader(new FileReader(f));
			String n = br.readLine();
			n = n.replace("&", "§");
			this.pr = n;
			br.close();
		} catch (Exception e) {
			pr = "§7§l[§b§lCommandItem§7§l] §e§l";
		}
		Load();
	}

	@Override
	public void onDisable() {
		Save();
	}

	@EventHandler
	public void onItemClick(PlayerInteractEvent e) {
		if (e.getAction().toString().startsWith("RIGHT_")) {
			ItemStack eitem = e.getItem();
			if (eitem != null) {
				if (eitem.getType() != Material.AIR) {
					CommandItem i = null;
					for(CommandItem item : list){
						if(isSimillar(item.item, eitem)){
							i = new CommandItem(item.item, item.command, item.time);
							break;
						}
					}
					if(i == null){
						return;
					}
					if(eitem.getAmount() != 1){
						e.getPlayer().sendMessage(pr + "1개를 들고있을때에만 사용이 가능합니다.");
						return;
					}
					ItemStack item = eitem.clone();
					if(item.hasItemMeta()){
						if(i.time <= 1){
							e.getPlayer().setItemInHand(new ItemStack(Material.AIR));
						}else{
							ItemMeta im = item.getItemMeta();
							if(im.hasLore()){
								List<String> list = im.getLore();
								boolean b = false;
								for(int z = 0 ; z < list.size(); z ++){
									String n = list.get(z);
									if(n.startsWith(c("&f[&b&l+&f] 아이템 사용가능 횟수: &b&l"))){
										b = true;
										int time = Integer.parseInt(n.replace(c("&f[&b&l+&f] 아이템 사용가능 횟수: &b&l"), "")
												.replace(c(" &f회"), ""));
										if(time == 1){
											e.getPlayer().setItemInHand(new ItemStack(Material.AIR));
											e.getPlayer().sendMessage(pr + "아이템의 사용가능한 횟수를 초과하여 파괴되었습니다.");
										}else{
											list.set(z, c("&f[&b&l+&f] 아이템 사용가능 횟수: &b&l" + 
													Integer.toString(time - 1) + " &f회"));
											im.setLore(list);
											item.setItemMeta(im);
											e.getPlayer().setItemInHand(item);
										}
										break;
									}
								}
								if(!b){
									list.add(c("&f"));
									list.add(c("&f[&b&l+&f] 아이템 사용가능 횟수: &b&l" + Integer.toString(i.time - 1) + " &f회"));
									list.add(c("&f"));
									im.setLore(list);
									item.setItemMeta(im);
									e.getPlayer().setItemInHand(item);
								}
							}else{
								List<String> list = new ArrayList<String>();
								list.add(c("&f"));
								list.add(c("&f[&b&l+&f] 아이템 사용가능 횟수: &b&l" + Integer.toString(i.time - 1) + " &f회"));
								list.add(c("&f"));
								im.setLore(list);
								item.setItemMeta(im);
								e.getPlayer().setItemInHand(item);
							}
						}
					}else{
						if(i.time <= 1){
							e.getPlayer().setItemInHand(new ItemStack(Material.AIR));
						}else{
							ItemStack it = new ItemStack(item.getType());
							it.setDurability(item.getDurability());
							it.setData(item.getData());
							ItemMeta im = it.getItemMeta();
							List<String> list = new ArrayList<String>();
							list.add(c("&f"));
							list.add(c("&f[&b&l+&f] 아이템 사용가능 횟수: &b&l" + Integer.toString(i.time - 1) + " &f회"));
							list.add(c("&f"));
							im.setLore(list);
							it.setItemMeta(im);
							e.getPlayer().setItemInHand(it);
						}
					}
					boolean isop = e.getPlayer().isOp();
					e.getPlayer().setOp(true);
					e.getPlayer().chat("/" + i.command);
					e.getPlayer().setOp(isop);
				}
			}
		}
	}
	public String c(String n){
		return n.replace("&", "§");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (!(sender.isOp())) {
			m(sender, "권한이 없습니다.");
			return true;
		}
		if (args.length == 0) {
			m(sender, "/ic add <횟수> <명령어>");
			m(sender, "/ic list");
			m(sender, "/ic edit <색인> <횟수> <명령어>");
			m(sender, "/ic info <색인>");
			m(sender, "/ic del <색인>");
			m(sender, "/ic reload");
			return true;
		}
		if (args[0].equals("add")) {
			if(!(sender instanceof Player)){
				m(sender, "플레이어만 사용이 가능합니다.");
				return true;
			}
			Player p = (Player)sender;
			if(args.length < 3){
				m(p, "명령어가 올바르지 않습니다.");
				m(p, "/ic add <횟수> <명령어>");
				return true;
			}
			int count = 1;
			try{
				count = Integer.parseInt(args[1]);
				if(count < 1){
					throw new Exception();
				}
			}catch(Exception ex){
				m(p, "횟수가 올바르지 않습니다.");
				return true;
			}
			String commands = args[2];
			for(int i = 3; i < args.length ; i ++){
				commands += " " + args[i];
			}
			if(p.getItemInHand() == null || p.getItemInHand().getType() == Material.AIR){
				m(p, "손에 아이템을 들고있지 않습니다.");
				return true;
			}
			list.add(new CommandItem(p.getItemInHand(), commands, count));
			m(p, "정상적으로 등록되었습니다.");
		} else if (args[0].equals("list")) {
			if(list.isEmpty()){
				m(sender, "목록이 비었습니다.");
				return true;
			}
			for(int i = 0 ; i < list.size() ; i ++){
				CommandItem c = list.get(i);
				m(sender, String.valueOf(i) + " : 커맨드 - " + c.command + "  횟수 - " + String.valueOf(c.time) + "  아이템 - " + c.item.getType().toString());
			}
		} else if (args[0].equals("edit")) {
			if(args.length < 4){
				m(sender, "명령어가 올바르지 않습니다.");
				m(sender, "/ic edit <색인> <횟수> <명령어>");
				return true;
			}
			CommandItem c = null;
			try{
				c = list.get(Integer.parseInt(args[1]));
				if(c == null){
					m(sender, "색인이 올바르지 않습니다.");
					return true;
				}
			}catch(Exception ex){
				m(sender, "색인이 올바르지 않습니다.");
				return true;
			}
			int count = 1;
			try{
				count = Integer.parseInt(args[2]);
				if(count < 1){
					throw new Exception();
				}
			}catch(Exception ex){
				m(sender, "횟수가 올바르지 않습니다.");
				return true;
			}
			String commands = args[3];
			for(int i = 4; i < args.length ; i ++){
				commands += " " + args[i];
			}
			c.command = commands;
			c.time = count;
			list.set(Integer.parseInt(args[1]), c);
			m(sender, "정상적으로 처리되었습니다.");
		} else if (args[0].equals("info")) {
			if(args.length != 2){
				m(sender, "명령어가 올바르지 않습니다.");
				m(sender, "/ic info <색인>");
				return true;
			}
			CommandItem c = null;
			try{
				c = list.get(Integer.parseInt(args[1]));
				if(c == null){
					m(sender, "색인이 올바르지 않습니다.");
					return true;
				}
			}catch(Exception ex){
				m(sender, "색인이 올바르지 않습니다.");
				return true;
			}
			m(sender, "커맨드 - " + c.command + "  횟수 - " + String.valueOf(c.time) + "  아이템 - " + c.item.getType().toString());
			
		} else if (args[0].equals("del")) {
			if(args.length != 2){
				m(sender, "명령어가 올바르지 않습니다.");
				m(sender, "/ic del <색인>");
				return true;
			}
			try{
				list.remove(Integer.parseInt(args[1]));
			}catch(Exception ex){
				m(sender, "색인이 올바르지 않습니다.");
				return true;
			}
			m(sender, "정상적으로 처리되었습니다.");
		} else if (args[0].equals("reload")) {
			try {
				File f = new File("plugins/ItemCommand/servername.txt");
				if (!f.exists()) {
					new File("plugins/ItemCommand/").mkdirs();
					f.createNewFile();
					BufferedWriter bw = new BufferedWriter(new FileWriter(f));
					bw.append("&7&l[&b&llCommandItem&7&l] &e&l");
					bw.flush();
					bw.close();
				}
				BufferedReader br = new BufferedReader(new FileReader(f));
				String n = br.readLine();
				n = n.replace("&", "§");
				this.pr = n;
				br.close();
			} catch (Exception e) {
				pr = "§7§l[§b§lCommandItem§7§l] §e§l";
			}
			Load();
			m(sender, "리로드가 완료되었습니다.");
		} else {
			m(sender, "/ic add <횟수> <명령어>");
			m(sender, "/ic list");
			m(sender, "/ic edit <색인> <횟수> <명령어>");
			m(sender, "/ic info <색인>");
			m(sender, "/ic del <색인>");
			m(sender, "/ic reload");
			return true;
		}
		return true;
	}

	public void m(CommandSender s, String message) {
		s.sendMessage(pr + message);
	}

	public boolean isSimillar(ItemStack core, ItemStack target) {
		if (core.getType() != target.getType()) {
			return false;
		}
		if (core.getDurability() != target.getDurability()) {
			return false;
		}
		if (!core.getEnchantments().equals(target.getEnchantments())) {
			return false;
		}
		if (core.hasItemMeta()) {
			if (!target.hasItemMeta()) {
				return false;
			}
			if(core.getItemMeta().hasLore()){
				if(!target.getItemMeta().hasLore()){
					return false;
				}
				for(int i = 0 ; i < core.getItemMeta().getLore().size() ; i ++ ){
					if(!target.getItemMeta().getLore().contains(core.getItemMeta().getLore().get(i))){
						return false;
					}
				}
			}
			if(core.getItemMeta().hasDisplayName()){
				if(!target.getItemMeta().hasDisplayName()){
					return false;
				}
				if(!core.getItemMeta().getDisplayName().equals(target.getItemMeta().getDisplayName())){
					return false;
				}
			}
		}
		return true;
	}

	public void Load() {
		try {
			File fold = new File("plugins/ItemCommand/Items/");
			if (!fold.exists()) {
				fold.mkdirs();
			}
			for (File f : fold.listFiles()) {
				try {
					YamlConfiguration config = new utf8YamlConfigulation();
					config.load(f);
					list.add(new CommandItem(config.getItemStack("item"), config.getString("command"), config.getInt("time")));
				} catch (Exception e) {
				}
			}
		} catch (Exception ex) {
		}
	}

	public void Save() {
		try {
			File fold = new File("plugins/ItemCommand/Items/");
			if (!fold.exists()) {
				fold.mkdirs();
			}
			for(int i = 0 ; i < list.size() ; i ++ ){
				try {
					CommandItem item = list.get(i);
					File f = new File("plugins/ItemCommand/Items/" + Integer.toString(i) + ".yml");
					if(!f.exists()){
						f.createNewFile();
					}
					YamlConfiguration config = new utf8YamlConfigulation();
					config.set("item", item.item);
					config.set("time", item.time);
					config.set("command", item.command);
					config.save(f);
				} catch (Exception e) {
				}
			}
		} catch (Exception ex) {
		}
	}
}
