package com.modrinth.minotaur;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import java.util.ArrayList;
import java.util.List;

import static com.modrinth.minotaur.Util.ext;

/**
 * The main class for Minotaur.
 */
public class Minotaur implements Plugin<Project> {
    /**
     * Creates the {@link ModrinthExtension} for the project and registers the {@code modrinth} and
     * {@code modrinthSyncBody} tasks.
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
            ModrinthExtension extension = ext(evaluatedProject);

            if (!extension.getAutoAddDependsOn().getOrElse(true)) {
                return;
            }

            Task task = evaluatedProject.getTasks().getByName("modrinth");
            List<Object> candidateAATs = new ArrayList<>();

            candidateAATs.add(extension.getUploadFile().getOrNull());
            candidateAATs.addAll(extension.getAdditionalFiles().get());

            candidateAATs.forEach(file -> {
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

        project.getLogger().debug("Successfully applied the Modrinth plugin!");
    }
}
