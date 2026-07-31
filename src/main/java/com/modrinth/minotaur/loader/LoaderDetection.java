package com.modrinth.minotaur.loader;

import org.gradle.api.Project;
import org.gradle.api.plugins.PluginManager;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import java.util.*;

@ApiStatus.Internal
public class LoaderDetection {
	private static final LinkedHashMap<String, String> pluginLoaderMap = new LinkedHashMap<>();

	static {
		pluginLoaderMap.put("net.minecraftforge.gradle", "forge");
		pluginLoaderMap.put("net.neoforged.gradle", "neoforge");
		pluginLoaderMap.put("net.neoforged.gradle.userdev", "neoforge");
		pluginLoaderMap.put("net.neoforged.moddev", "neoforge");
		pluginLoaderMap.put("net.neoforged.moddev.legacyforge", "forge");
		pluginLoaderMap.put("org.quiltmc.loom", "quilt");
		pluginLoaderMap.put("org.spongepowered.gradle.plugin", "sponge");
		pluginLoaderMap.put("io.papermc.paperweight.userdev", "paper");
		pluginLoaderMap.put("xyz.jpenilla.run-paper", "paper");
		pluginLoaderMap.put("xyz.jpenilla.run-waterfall", "waterfall");
		pluginLoaderMap.put("xyz.jpenilla.run-velocity", "velocity");
	}

	private LoaderDetection() {
		throw new UnsupportedOperationException();
	}

	public static List<String> detectLoaders(Project project) {
		Set<String> loaders = new LinkedHashSet<>();
		PluginManager pluginManager = project.getPluginManager();
		Logger logger = project.getLogger();
		pluginLoaderMap.forEach((plugin, loader) -> {
			if (pluginManager.hasPlugin(plugin) && loaders.add(loader)) {
				logger.debug("Adding loader '{}' because plugin '{}' was found.", loader, plugin);
			}
		});

		if (!loaders.contains("quilt") // don't count quilt-loom twice
			&& project.getExtensions().findByName("loom") != null) {
			Object loomPlatform = project.findProperty("loom.platform");
			if (loomPlatform instanceof String) {
				logger.debug("Adding loader '{}' because 'loom' extension was found and loom.platform={}.", loomPlatform, loomPlatform);
				loaders.add((String) loomPlatform);
			} else {
				logger.debug("Adding loader 'fabric' because 'loom' extension was found.");
				loaders.add("fabric");
			}
		}

		return new ArrayList<>(loaders);
	}
}
