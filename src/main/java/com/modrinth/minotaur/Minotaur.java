package com.modrinth.minotaur;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.util.Collections;

/**
 * The main class for Minotaur.
 */
@SuppressWarnings("unused")
public class Minotaur implements Plugin<Project> {
    /**
     * Does the following:
     * <ol>
     *   <li>Creates the {@link ModrinthExtension} for the project</li>
     *   <li>Waits until the project is done evaluating</li>
     *   <li>Registers the {@code modrinth} task</li>
     *   <li>Detects the game versions for Fabric and Forge</li>
     *   <li>Sets default values that couldn't be done without an evaluated project (version number and name)</li>
     *   <li>Prints a debug line saying it's done!</li>
     * </ol>
     * @param project The Gradle project which Minotaur is applied to
     */
    @Override
    public void apply(final Project project) {
        project.getExtensions().create("modrinth", ModrinthExtension.class, project);

        project.afterEvaluate(evaluatedProject -> {
            final Task mainTask = evaluatedProject.getTasks().create("modrinth", TaskModrinthUpload.class);
            mainTask.setGroup("publishing");
            mainTask.setDescription("Upload project to Modrinth");
            mainTask.setMustRunAfter(Collections.singleton(evaluatedProject.getTasks().getByName("build")));

            TaskModrinthUpload.detectGameVersionForge(evaluatedProject);
            TaskModrinthUpload.detectGameVersionFabric(evaluatedProject);

            final ModrinthExtension extension = evaluatedProject.getExtensions().getByType(ModrinthExtension.class);
            if (extension.getVersionNumber().getOrNull() == null) {
                extension.getVersionNumber().set(evaluatedProject.getVersion().toString());
            }
            extension.getVersionName().set(extension.getVersionNumber().get());
        });

        project.getLogger().debug("Successfully applied the Modrinth plugin!");
    }
}