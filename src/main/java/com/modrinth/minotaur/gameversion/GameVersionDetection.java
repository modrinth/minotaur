package com.modrinth.minotaur.gameversion;

import io.papermc.paperweight.userdev.PaperweightUserExtension;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.api.plugins.PluginManager;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import java.util.*;

@ApiStatus.Internal
public class GameVersionDetection {
	private GameVersionDetection() {
		throw new UnsupportedOperationException();
	}

	public static List<String> detectGameVersions(Project project) {
		Logger logger = project.getLogger();
		PluginManager pluginManager = project.getPluginManager();

		LinkedHashSet<String> versions = new LinkedHashSet<>();

		if (pluginManager.hasPlugin("net.minecraftforge.gradle") ||
			pluginManager.hasPlugin("net.neoforged.gradle") ||
			pluginManager.hasPlugin("net.neoforged.gradle.userdev")) {

			String[] props = {"MC_VERSION", "minecraftVersion"};

			ExtraPropertiesExtension extraProperties = project.getExtensions().getExtraProperties();
			for (String prop : props) {
				try {
					String version = (String) extraProperties.get(prop);
					if (version != null) {
						logger.debug("Adding fallback game version {} from ForgeGradle/NeoGradle.", version);
						versions.add(version);
						break;
					}
				} catch (Exception e) {
					logger.debug("Could not find property {}", prop);
				}
			}
		}

		if (project.getExtensions().findByName("loom") != null) {
			// Get the version from the first dependency in the "minecraft" configuration, similar to how Loom does it.
			// https://github.com/FabricMC/fabric-loom/blob/97f594da8e132c3d33cf39fe8d7cc0e76d84aeb6/src/main/java/net/fabricmc/loom/configuration/DependencyInfo.java#LL60C26-L60C56
			Optional.ofNullable(project.getConfigurations().findByName("minecraft"))
				.map(m -> m.getDependencies().iterator())
				.filter(Iterator::hasNext)
				.map(i -> i.next().getVersion())
				.ifPresent(version -> {
					project.getLogger().debug("Adding fallback game version {} from Loom.", version);
					versions.add(version);
				});
		}

		if (project.getExtensions().findByName("paperweight") != null) {
			String mcVer = project.getExtensions().getByType(PaperweightUserExtension.class).getMinecraftVersion().getOrNull();
			if (mcVer != null) {
				logger.debug("Adding fallback game version {} from paperweight-userdev.", mcVer);
				versions.add(mcVer);
			}
		}

		return new ArrayList<>(versions);
	}
}
