package com.modrinth.minotaur;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;

/**
 * The main class for Minotaur.
 */
public class Minotaur implements Plugin<Project> {
    /**
     * Creates the {@link ModrinthExtension} for the project and registers the {@code modrinth} task.
     * Also registers the `modrinthSyncBody` task if `syncBodyFrom` is specified in {@link ModrinthExtension}.
     * @param project The Gradle project which Minotaur is applied to
     */
    @Override
    public void apply(final Project project) {
        project.getExtensions().create("modrinth", ModrinthExtension.class, project);
        project.getLogger().debug("Created the `modrinth` extension.");

        TaskContainer tasks = project.getTasks();
        tasks.register("modrinth", TaskModrinthUpload.class, task -> {
            task.setGroup("publishing");
            task.setDescription("Upload project to Modrinth");
            task.dependsOn(tasks.named("build"));
        });
        project.getLogger().debug("Registered the `modrinth` task.");

        tasks.register("modrinthSyncBody", TaskModrinthSyncBody.class, task -> {
            task.setGroup("publishing");
            task.setDescription("Sync project description to Modrinth");
        });
        project.getLogger().debug("Registered the `modrinthSyncBody` task.");

        project.getLogger().debug("Successfully applied the Modrinth plugin!");
    }
}
