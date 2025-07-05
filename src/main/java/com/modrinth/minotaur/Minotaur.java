package com.modrinth.minotaur;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

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
		ModrinthExtension extension =
			project.getExtensions().create("modrinth", ModrinthExtension.class, project);
		project.getLogger().debug("Created the `modrinth` extension.");

		TaskContainer tasks = project.getTasks();
		tasks.register("modrinth", TaskModrinthUpload.class, task -> {
			task.setGroup("publishing");
			task.setDescription("Upload project to Modrinth");
			task.dependsOn(tasks.named("assemble"));
			task.mustRunAfter(tasks.named("build"));
			task.getModrinthExtension().set(extension);
			task.getProjectVersion().set(project.getVersion().toString());
			task.getPluginManager().set(project.getPluginManager());
			task.getExtensionContainer().set(project.getExtensions());

			if (project.findProperty("loom.platform") != null) {
				task.getLoomPlatform().set((String) project.findProperty("loom.platform"));
			}

			if (extension.getGameVersions().get().isEmpty()) {
				try {
					task.getLoomMinecraftVersion().set(
						project.getConfigurations().getByName("minecraft")
							.getDependencies().iterator().next().getVersion()
					);
				} catch (Exception e) {
					// do nothing
				}
			}
		});
		project.getLogger().debug("Registered the `modrinth` task.");

		tasks.register("modrinthSyncBody", TaskModrinthSyncBody.class, task -> {
			task.setGroup("publishing");
			task.setDescription("Sync project description to Modrinth");
			task.getModrinthExtension().set(extension);
		});
		project.getLogger().debug("Registered the `modrinthSyncBody` task.");

		project.afterEvaluate(evaluatedProject -> {
			ModrinthExtension ext = evaluatedProject.getExtensions().findByType(ModrinthExtension.class);

			if (!ext.getAutoAddDependsOn().getOrElse(true)) {
				return;
			}

			evaluatedProject.getTasks().named("modrinth", TaskModrinthUpload.class).configure(task -> {
				task.getWiredInputFiles().from(ext.getFile());
				task.getInputs().property("changelog", ext.getChangelog()).optional(true);

				ext.getAdditionalFiles().get().forEach(file -> {
					if (file == null) {
						return;
					}

					// Try to get an AbstractArchiveTask from the input file by whatever means possible.
					if (file instanceof AbstractArchiveTask) {
						task.dependsOn(file);
					} else if (file instanceof TaskProvider<?> &&
						((TaskProvider<?>) file).get() instanceof AbstractArchiveTask) {
						task.dependsOn(((TaskProvider<?>) file).get());
					}
				});

				evaluatedProject.getLogger().debug("Made the `modrinth` task depend on the upload file and additional files.");
			});
		});

		project.getLogger().debug("Successfully applied the Modrinth plugin!");
	}
}
