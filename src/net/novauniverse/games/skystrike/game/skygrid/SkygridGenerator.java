package net.novauniverse.games.skystrike.game.skygrid;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.commons.tasks.Task;
import net.zeeraa.novacore.spigot.tasks.SimpleTask;

public class SkygridGenerator {
	private Random random;
	private World world;
	private int x;
	private int size;

	private boolean completed;

	public static final int DENSITY = 4;

	private SimpleTask generatorTask;

	public SkygridGenerator(Plugin plugin, World world, Random random, int size) {
		this.random = random;
		this.world = world;
		this.size = size;
		
		this.x = 0;
		this.completed = false;
		this.generatorTask = new SimpleTask(plugin, new Runnable() {
			@Override
			public void run() {
				generateNext();
			}
		}, 1L, 1L);
	}

	public void start() {
		if (completed) {
			return;
		}
		Task.tryStartTask(generatorTask);
		Log.info("SkygridGenerator", "World generation started");
	}

	private void generateNext() {
		x += SkygridGenerator.DENSITY;

		if (x > size) {
			Task.tryStopTask(generatorTask);
			Log.info("SkygridGenerator", "World generation completed");
			completed = true;
			return;
		}

		Log.trace("SkygridGenerator", "Running generation pass on x: " + x);

		for (int y = 1; y < world.getMaxHeight(); y += SkygridGenerator.DENSITY) {
			if (y > world.getMaxHeight()) {
				// idk if this will ever be
				return;
			}
			for (int z = 0; z < size; z += 4) {
				Location location = new Location(world, x, y, z);
				Material material = SkyStrikeMaterials.GROUND_MATERIALS.get(this.random.nextInt(SkyStrikeMaterials.GROUND_MATERIALS.size()));

				location.getBlock().setType(material, false);
			}
		}
	}

	public boolean isCompleted() {
		return completed;
	}
}