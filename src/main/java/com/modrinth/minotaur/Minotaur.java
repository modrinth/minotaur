package com.modrinth.minotaur;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;

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

		TaskContainer tasks = project.getTasks();
		tasks.register("modrinth", TaskModrinthUpload.class, task -> {
			task.setGroup("publishing");
			task.setDescription("Upload project to Modrinth");
			task.dependsOn(tasks.named("assemble"));
			task.mustRunAfter(tasks.named("build"));
			task.notCompatibleWithConfigurationCache("Fundamentally incompatible with configuration cache");

			task.getFile().set(ext.getFile());
			task.getAdditionalFiles().set(ext.getAdditionalFileDsl().getAdditionalFiles());
			task.getUntypedAdditionalFiles().from(ext.getAdditionalFiles());
			task.getChangelog().set(ext.getChangelog());
			task.getFailSilently().set(ext.getFailSilently());
		});
		project.getLogger().debug("Registered the `modrinth` task.");

		tasks.register("modrinthSyncBody", TaskModrinthSyncBody.class, task -> {
			task.setGroup("publishing");
			task.setDescription("Sync project description to Modrinth");
			task.notCompatibleWithConfigurationCache("Fundamentally incompatible with configuration cache");
		});
		project.getLogger().debug("Registered the `modrinthSyncBody` task.");
		project.getLogger().debug("Successfully applied the Modrinth plugin!");
	}
}
