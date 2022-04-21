package net.novauniverse.games.skystrike;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONException;
import org.json.JSONObject;

import net.novauniverse.games.skystrike.game.SkyStrike;
import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.commons.utils.JSONFileUtils;
import net.zeeraa.novacore.spigot.NovaCore;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.GameManager;
import net.zeeraa.novacore.spigot.gameengine.module.modules.gamelobby.GameLobby;
import net.zeeraa.novacore.spigot.module.ModuleManager;
import net.zeeraa.novacore.spigot.module.modules.compass.CompassTracker;

public class NovaSkyStrike extends JavaPlugin implements Listener {
	private static NovaSkyStrike instance;

	private boolean allowReconnect;
	private boolean combatTagging;
	private int reconnectTime;
	
	public boolean isAllowReconnect() {
		return allowReconnect;
	}

	public boolean isCombatTagging() {
		return combatTagging;
	}

	public int getReconnectTime() {
		return reconnectTime;
	}

	private SkyStrike game;

	public static NovaSkyStrike getInstance() {
		return instance;
	}

	public SkyStrike getGame() {
		return game;
	}

	@Override
	public void onEnable() {
		NovaSkyStrike.instance = this;

		saveDefaultConfig();

		allowReconnect = getConfig().getBoolean("allow_reconnect");
		combatTagging = getConfig().getBoolean("combat_tagging");
		reconnectTime = getConfig().getInt("player_elimination_delay");

		GameManager.getInstance().setUseCombatTagging(combatTagging);
		
		File lootTableFolder = new File(this.getDataFolder().getPath() + File.separator + "LootTables");
		File mapOverrides = new File(this.getDataFolder().getPath() + File.separator + "map_overrides.json");
		if (mapOverrides.exists()) {
			Log.info("Trying to read map overrides file");
			try {
				JSONObject mapFiles = JSONFileUtils.readJSONObjectFromFile(mapOverrides);

				boolean relative = mapFiles.getBoolean("relative");

				lootTableFolder = new File((relative ? this.getDataFolder().getPath() + File.separator : "") + mapFiles.getString("loot_tables_folder"));

				Log.info("New paths:");
				Log.info("Loot table folder:" + lootTableFolder.getAbsolutePath());
			} catch (JSONException | IOException e) {
				e.printStackTrace();
				Log.error("Failed to read map overrides from file " + mapOverrides.getAbsolutePath());
			}
		}

		try {
			FileUtils.forceMkdir(getDataFolder());
			FileUtils.forceMkdir(lootTableFolder);
		} catch (IOException e1) {
			e1.printStackTrace();
			Log.fatal("Skywars", "Failed to setup data directory");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		// Enable required modules
		ModuleManager.enable(GameManager.class);
		ModuleManager.enable(GameLobby.class);
		ModuleManager.enable(CompassTracker.class);

		// Init game
		this.game = new SkyStrike(this);

		GameManager.getInstance().loadGame(game);

		// Register events
		Bukkit.getServer().getPluginManager().registerEvents(this, this);

		// Load loot tables
		NovaCore.getInstance().getLootTableManager().loadAll(lootTableFolder);
	}

	@Override
	public void onDisable() {
	}
}