package net.novauniverse.games.skystrike.game;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.World.Environment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import net.novauniverse.games.skystrike.NovaSkyStrike;
import net.novauniverse.games.skystrike.game.skygrid.SkygridGenerator;
import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.Game;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.GameEndReason;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.elimination.PlayerQuitEliminationAction;
import net.zeeraa.novacore.spigot.module.modules.multiverse.MultiverseManager;
import net.zeeraa.novacore.spigot.module.modules.multiverse.PlayerUnloadOption;
import net.zeeraa.novacore.spigot.module.modules.multiverse.WorldOptions;
import net.zeeraa.novacore.spigot.module.modules.multiverse.WorldUnloadOption;

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

	@Override
	public void onStart() {
		if (started) {
			return;
		}
		started = true;
	}

	@Override
	public void onEnd(GameEndReason reason) {
		if (ended) {
			return;
		}
		ended = true;
	}

}
