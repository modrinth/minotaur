package com.modrinth.minotaur;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.jetbrains.annotations.ApiStatus;

import static com.modrinth.minotaur.Util.getExtension;

/**
 * The main class for Minotaur.
 */
public class Minotaur implements Plugin<Project> {
    /**
     * Internal utility for grabbing the project which Minotaur is applied to
     */
    @ApiStatus.Internal
    public static Project project;

    /**
     * Creates the {@link ModrinthExtension} for the project and registers the {@code modrinth} and
     * {@code modrinthSyncBody} tasks.
     * @param project The Gradle project which Minotaur is applied to
     */
    @Override
    public void apply(final Project project) {
        Minotaur.project = project;

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
            ModrinthExtension extension = getExtension();
            Task task = evaluatedProject.getTasks().getByName("modrinth");
            if (extension.getUploadFile().getOrNull() != null) {
                Object uploadFile = extension.getUploadFile().get();
                // We have an upload file set. Try to get an AbstractArchiveTask from it by whatever means possible.
                if (uploadFile instanceof AbstractArchiveTask) {
                    task.dependsOn(uploadFile);
                } else if (uploadFile instanceof TaskProvider<?> &&
                    ((TaskProvider<?>) uploadFile).get() instanceof AbstractArchiveTask) {
                    task.dependsOn(((TaskProvider<?>) uploadFile).get());
                }
            }
            for (Object file : extension.getAdditionalFiles().get()) {
                if (file instanceof AbstractArchiveTask) {
                    task.dependsOn(file);
                } else if (file instanceof TaskProvider<?> &&
                    ((TaskProvider<?>) file).get() instanceof AbstractArchiveTask) {
                    task.dependsOn(((TaskProvider<?>) file).get());
                }
            }
        });
        project.getLogger().debug("Made the `modrinth` task depend on the upload file and additional files.");

        project.getLogger().debug("Successfully applied the Modrinth plugin!");
    }
}
