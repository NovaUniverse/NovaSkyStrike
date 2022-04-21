package net.novauniverse.games.skystrike.game;

import java.io.File;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.novauniverse.games.skystrike.NovaSkyStrike;
import net.novauniverse.games.skystrike.game.skygrid.SkygridGenerator;
import net.novauniverse.games.skystrike.game.starteritems.SkyStrikeStarterItems;
import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.Game;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.GameEndReason;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.elimination.PlayerQuitEliminationAction;
import net.zeeraa.novacore.spigot.module.modules.multiverse.MultiverseManager;
import net.zeeraa.novacore.spigot.module.modules.multiverse.PlayerUnloadOption;
import net.zeeraa.novacore.spigot.module.modules.multiverse.WorldOptions;
import net.zeeraa.novacore.spigot.module.modules.multiverse.WorldUnloadOption;
import net.zeeraa.novacore.spigot.utils.PlayerUtils;

public class SkyStrike extends Game {
	private boolean started;
	private boolean ended;

	public static final int MAP_SIZE = 250;
	public static final String WORLD_NAME = "skystrike";

	private SkygridGenerator generator;

	public SkyStrike(Plugin plugin) {
		super(plugin);

		this.generator = null;
		this.started = false;
		this.ended = true;
	}

	@Override
	public String getName() {
		return "skystrike";
	}

	@Override
	public String getDisplayName() {
		return "SkyStrike";
	}

	@Override
	public PlayerQuitEliminationAction getPlayerQuitEliminationAction() {
		return NovaSkyStrike.getInstance().isAllowReconnect() ? PlayerQuitEliminationAction.DELAYED : PlayerQuitEliminationAction.INSTANT;
	}

	@Override
	public int getPlayerEliminationDelay() {
		return NovaSkyStrike.getInstance().getReconnectTime();
	}

	@Override
	public boolean eliminateIfCombatLogging() {
		return NovaSkyStrike.getInstance().isCombatTagging();
	}

	@Override
	public boolean eliminatePlayerOnDeath(Player player) {
		return true;
	}

	@Override
	public boolean isPVPEnabled() {
		return true;
	}

	@Override
	public boolean autoEndGame() {
		return true;
	}

	@Override
	public boolean hasStarted() {
		return started;
	}

	@Override
	public boolean hasEnded() {
		return ended;
	}

	@Override
	public boolean isFriendlyFireAllowed() {
		return false;
	}

	@Override
	public boolean canAttack(LivingEntity attacker, LivingEntity target) {
		return true;
	}

	@Override
	public boolean canStart() {
		if (generator != null) {
			return this.generator.isCompleted();
		}
		return false;
	}

	@Override
	public void onLoad() {
		File oldWorld = new File(Bukkit.getWorldContainer().getAbsolutePath() + File.separator + WORLD_NAME);
		if (oldWorld.exists()) {
			oldWorld.delete();
			Log.info("Deleting old world named " + WORLD_NAME);
		}

		WorldOptions options = new WorldOptions(WORLD_NAME);
		options.setPlayerUnloadOption(PlayerUnloadOption.KICK);
		options.setLockWeather(true);
		options.setGenerator("SimpleVoidGen");
		options.unloadOptions(WorldUnloadOption.DELETE);
		options.generateStructures(false);
		options.withEnvironment(Environment.NORMAL);
		this.world = MultiverseManager.getInstance().createWorld(options).getWorld();

		int borderSize = MAP_SIZE;
		if (borderSize % 2 != 0) {
			borderSize++;
		}
		world.getWorldBorder().setSize(borderSize);
		world.getWorldBorder().setCenter(MAP_SIZE / 2, MAP_SIZE / 2);

		generator = new SkygridGenerator(getPlugin(), world, random, MAP_SIZE);
		generator.start();
	}

	@Nullable
	public Location generateSpawnLocation() {
		int maxSize = MAP_SIZE / SkygridGenerator.DENSITY;
		for (int i = 0; i < 1000; i++) {
			int x = random.nextInt(maxSize) * SkygridGenerator.DENSITY;
			int z = random.nextInt(maxSize) * SkygridGenerator.DENSITY;

			int y = 1 + (SkygridGenerator.DENSITY * 16);

			Location location = new Location(world, x, y, z);
			Block block = location.getBlock();

			Log.trace("SkyStrike", "Attempting to get spawn location at X: " + x + " Y: " + y + " Z: " + z + " in world: " + world.getName() + ". Block material is " + block.getType().name());

			if (block.getType() != Material.AIR) {
				if (!block.isLiquid()) {
					if (block.getType().isSolid()) {
						location.add(0, 1.5, 0);
						return location;
					}
				}
			}
		}
		return null;
	}

	@Override
	public void onStart() {
		if (started) {
			return;
		}
		started = true;

		Location spectatorLocation = new Location(world, MAP_SIZE / 2, world.getMaxHeight(), MAP_SIZE / 2);

		Bukkit.getServer().getOnlinePlayers().forEach(player -> {
			PlayerUtils.clearPlayerInventory(player);
			PlayerUtils.clearPotionEffects(player);
			PlayerUtils.resetPlayerXP(player);
			PlayerUtils.fullyHealPlayer(player);
			if (players.contains(player.getUniqueId())) {
				SkyStrikeStarterItems.STARTER_ITEMS.forEach(item -> player.getInventory().addItem(item.clone()));
				player.setGameMode(GameMode.SURVIVAL);

				Location location = this.generateSpawnLocation();
				if (location == null) {
					Log.error("SkyStrike", "Failed to spawn player " + player.getName() + ". Please spawn them manually");
					player.sendMessage(ChatColor.RED + "An error occured while trying to spawn you in to the game. Please message and admin and they will teleport you to a starter location");
				} else {
					player.teleport(location);
					// Teleport again after 10 ticks to prevent some players from getting stuck
					new BukkitRunnable() {
						@Override
						public void run() {
							player.teleport(location);
						}
					}.runTaskLater(getPlugin(), 10L);
				}
			} else {
				player.setGameMode(GameMode.SPECTATOR);
				player.teleport(spectatorLocation);
			}
		});
	}

	@Override
	public void onEnd(GameEndReason reason) {
		if (ended) {
			return;
		}
		ended = true;
	}

}
