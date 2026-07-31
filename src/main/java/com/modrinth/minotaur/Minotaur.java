package com.modrinth.minotaur;

import com.modrinth.minotaur.dependencies.container.NamedDependency;
import com.modrinth.minotaur.request.ModrinthApiSettings;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.modrinth.minotaur.gameversion.GameVersionDetection.detectGameVersions;
import static com.modrinth.minotaur.loader.LoaderDetection.detectLoaders;

/**
 * The main class for Minotaur.
 */
public class Minotaur implements Plugin<Project> {
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

		ListProperty<String> defaultLoaders = project.getObjects().listProperty(String.class).empty();
		ListProperty<String> defaultGameVersions = project.getObjects().listProperty(String.class).empty();

		// Some of the plugins we inspect register their extensions *super* late
		// and this is the only thing that's late enough, as far as I can tell.
		project.getGradle().projectsEvaluated(g -> {
			defaultLoaders.set(detectLoaders(project));
			defaultGameVersions.set(detectGameVersions(project));
		});

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
			task.getLoaders().set(getOrDefaultLoaders(ext, defaultLoaders));
			task.getGameVersions().set(getOrDefaultGameVersions(ext, defaultGameVersions));
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
		settings.getToken().set(ext.getToken().orElse(ext.getDebugMode().map(d -> d ? "mrp-meow" : null)));
		settings.getProjectId().set(ext.getProjectId());
		settings.getVersionNumber().set(resolvedVersion);
	}

	private static Provider<List<String>> getOrDefaultLoaders(ModrinthExtension ext, Provider<List<String>> defaultLoaders) {
		Provider<List<String>> fallback = ext.getDetectLoaders()
			.zip(defaultLoaders, (detect, loaders) -> detect ? loaders : Collections.emptyList());
		return ext.getLoaders().map(l -> l.isEmpty() ? null : l).orElse(fallback);
	}

	private static Provider<List<String>> getOrDefaultGameVersions(ModrinthExtension ext, Provider<List<String>> detectedVersions) {
		return ext.getGameVersions().zip(detectedVersions, (v, def) -> v.isEmpty() ? def : v);
	}
}
