package net.novauniverse.games.skystrike.game.starteritems;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.zeeraa.novacore.spigot.utils.ItemBuilder;

public class SkyStrikeStarterItems {
	public static final List<ItemStack> STARTER_ITEMS = new ArrayList<>();
	
	static {
		STARTER_ITEMS.add(new ItemBuilder(Material.STONE_PICKAXE).setAmount(1).build());
		STARTER_ITEMS.add(new ItemBuilder(Material.STONE_AXE).setAmount(1).build());
		STARTER_ITEMS.add(new ItemBuilder(Material.STONE_SPADE).setAmount(1).build());
		STARTER_ITEMS.add(new ItemBuilder(Material.COBBLESTONE).setAmount(32).build());
	}
}