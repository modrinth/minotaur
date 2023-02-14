package com.modrinth.minotaur;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.UncheckedIOException;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.modrinth.minotaur.Util.ext;

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
		Path path = project.getRootProject().getProjectDir().toPath().resolve(".gradle").resolve("minotaur");
		if (Files.notExists(path)) {
			try {
				Files.createDirectories(path);
			} catch (IOException e) {
				throw new UncheckedIOException("Could not create path at " + path.toAbsolutePath(), e);
			}
		}
		File warningFile = path.resolve("2.7.0-warning-shown").toFile();
		if (!warningFile.exists()) {
			project.getLogger().warn("\n[Minotaur Warning] You are running a version of Minotaur that may contain " +
				"unintentional breaking changes.\n[Minotaur Warning] If a build of yours worked in v2.6.0 but broke in " +
				"v2.7.0, PLEASE report this immediately either via emailing support@modrinth.com or by opening a GitHub " +
				"issue on https://github.com/modrinth/minotaur/issues.\n[Minotaur Warning] This warning will not be " +
				"shown again.\n");
			try {
				//noinspection ResultOfMethodCallIgnored
				warningFile.createNewFile();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		project.getExtensions().create("modrinth", ModrinthExtension.class, project);
		project.getLogger().debug("Created the `modrinth` extension.");

		TaskContainer tasks = project.getTasks();
		tasks.register("modrinth", TaskModrinthUpload.class, task -> {
			task.setGroup("publishing");
			task.setDescription("Upload project to Modrinth");
			task.dependsOn(tasks.named("assemble"));
			task.mustRunAfter(tasks.named("build"));
		});
		project.getLogger().debug("Registered the `modrinth` task.");

		tasks.register("modrinthSyncBody", TaskModrinthSyncBody.class, task -> {
			task.setGroup("publishing");
			task.setDescription("Sync project description to Modrinth");
		});
		project.getLogger().debug("Registered the `modrinthSyncBody` task.");

		project.afterEvaluate(evaluatedProject -> {
			ModrinthExtension ext = ext(evaluatedProject);

			if (!ext.getAutoAddDependsOn().getOrElse(true)) {
				return;
			}

			evaluatedProject.getTasks().named("modrinth", TaskModrinthUpload.class).configure(task -> {
				task.getWiredInputFiles().from(ext.getUploadFileProperty());

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
