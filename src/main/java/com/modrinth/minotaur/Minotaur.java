package com.modrinth.minotaur;

import com.modrinth.minotaur.dependencies.container.NamedDependency;
import com.modrinth.minotaur.request.ModrinthApiSettings;
import io.papermc.paperweight.userdev.PaperweightUserExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The main class for Minotaur.
 */
public class Minotaur implements Plugin<Project> {
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

	/**
	 * Creates the {@link ModrinthExtension} for the project and registers the {@code modrinth} and
	 * {@code modrinthSyncBody} tasks.
	 *
	 * @param project The Gradle project which Minotaur is applied to
	 */
	@Override
	public void apply(final Project project) {
		ModrinthExtension ext = project.getExtensions().create("modrinth", ModrinthExtension.class);
		project.getLogger().debug("Created the `modrinth` extension.");

		TaskContainer tasks = project.getTasks();
		tasks.register("modrinth", TaskModrinthUpload.class, task -> {
			task.setGroup("publishing");
			task.setDescription("Upload project to Modrinth");
			task.dependsOn(tasks.named("assemble"));
			task.mustRunAfter(tasks.named("build"));

			task.getFile().set(ext.getFile());
			task.getAdditionalFiles().set(ext.getAdditionalFileDsl().getAdditionalFiles());
			task.getUntypedAdditionalFiles().from(ext.getAdditionalFiles());
			task.getChangelog().set(ext.getChangelog());
			task.getFailSilently().set(ext.getFailSilently());
			Provider<String> resolvedVersion = makeResolvedVersion(project, ext);
			task.getProjectId().set(ext.getProjectId());
			task.getVersionNumber().set(resolvedVersion);
			task.getVersionName().set(ext.getVersionName().orElse(task.getVersionNumber()));
			wireUpApiSettings(task.getApiSettings(), ext, resolvedVersion);
			task.getIsDryRun().set(ext.getDebugMode());
			task.getLoaders().set(getOrDetectLoaders(ext, project));
			task.getGameVersions().set(getOrDetectGameVersions(ext, project));
			task.getDependencies().set(ext.getDependencies().zip(ext.getNamedDependencies(), (deps, named) ->
				Stream.concat(
					named.stream().map(NamedDependency::getDependency),
					deps.stream()
				).collect(Collectors.toList())
			));
			task.getVersionType().set(ext.getVersionType());
		});
		project.getLogger().debug("Registered the `modrinth` task.");

		tasks.register("modrinthSyncBody", TaskModrinthSyncBody.class, task -> {
			task.setGroup("publishing");
			task.setDescription("Sync project description to Modrinth");

			wireUpApiSettings(task.getApiSettings(), ext, makeResolvedVersion(project, ext));
			task.getProjectId().set(ext.getProjectId());
			task.getSyncBodyFrom().set(ext.getSyncBodyFrom());
			task.getIsDryRun().set(ext.getDebugMode());
			task.getFailSilently().set(ext.getFailSilently());
		});
		project.getLogger().debug("Registered the `modrinthSyncBody` task.");
		project.getLogger().debug("Successfully applied the Modrinth plugin!");
	}

	private static Provider<String> makeResolvedVersion(Project project, ModrinthExtension ext) {
		return ext.getVersionNumber().orElse(project.getVersion().toString());
	}

	private static void wireUpApiSettings(ModrinthApiSettings settings, ModrinthExtension ext, Provider<String> resolvedVersion) {
		settings.getApiUrl().set(ext.getApiUrl());
		settings.getToken().set(ext.getToken());
		settings.getProjectId().set(ext.getProjectId());
		settings.getVersionNumber().set(resolvedVersion);
	}

	private static Provider<List<String>> getOrDetectLoaders(ModrinthExtension ext, Project project) {
		List<String> detectedLoaders = detectLoaders(project);
		Provider<List<String>> fallback = ext.getDetectLoaders()
			.map(detect -> detect ? detectedLoaders : Collections.emptyList());
		return ext.getLoaders().map(l -> l.isEmpty() ? null : l).orElse(fallback);
	}

	private static List<String> detectLoaders(Project project) {
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

	private static Provider<List<String>> getOrDetectGameVersions(ModrinthExtension ext, Project project) {
		List<String> detectedVersions = detectGameVersions(project);
		return ext.getGameVersions().map(v -> v.isEmpty() ? detectedVersions : v);
	}

	private static List<String> detectGameVersions(Project project) {
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
