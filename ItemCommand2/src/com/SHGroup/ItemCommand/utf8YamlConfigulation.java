package com.SHGroup.ItemCommand;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.io.Files;

public class utf8YamlConfigulation extends YamlConfiguration {

	public static Inventory loadInventoryFromYaml(File file) throws IOException,
			InvalidConfigurationException {
		YamlConfiguration yaml = new utf8YamlConfigulation();
		yaml.load(file);

		int inventorySize = yaml.getInt("size", 54);
		Inventory inventory = Bukkit.getServer().createInventory(null,
				inventorySize);

		ConfigurationSection items = yaml.getConfigurationSection("items");
		for (int slot = 0; slot < inventorySize; slot++) {
			String slotString = String.valueOf(slot);
			if (items.isItemStack(slotString)) {
				ItemStack itemStack = items.getItemStack(slotString);
				inventory.setItem(slot, itemStack);
			}
		}

		return inventory;
	}

	public static void saveInventoryToYaml(Inventory inventory, File file)
			throws IOException {
		YamlConfiguration yaml = new utf8YamlConfigulation();

		int inventorySize = inventory.getSize();
		yaml.set("size", Integer.valueOf(inventorySize));

		ConfigurationSection items = yaml.createSection("items");
		for (int slot = 0; slot < inventorySize; slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (stack != null) {
				items.set(String.valueOf(slot), stack);
			}
		}

		yaml.save(file);
	}
	@Override
	public void load(InputStream stream) throws IOException,
			InvalidConfigurationException {
		Validate.notNull(stream, "Stream cannot be null");
		InputStreamReader reader = new InputStreamReader(stream,
				Charset.forName("UTF-8"));
		StringBuilder builder = new StringBuilder();
		BufferedReader input = new BufferedReader(reader);
		try {
			String line;
			while ((line = input.readLine()) != null) {
				builder.append(line);
				builder.append('\n');
			}
		} finally {
			input.close();
		}

		loadFromString(builder.toString());
	}
	@Override
	public void save(File file) throws IOException {
		Validate.notNull(file, "File cannot be null");

		Files.createParentDirs(file);

		String data = saveToString();

		FileOutputStream stream = new FileOutputStream(file);
		OutputStreamWriter writer = new OutputStreamWriter(stream,
				Charset.forName("UTF-8"));
		try {
			writer.write(data);
		} finally {
			writer.close();
		}
	}
}