package com.modrinth.minotaur;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

@SuppressWarnings("unused")
public class Minotaur implements Plugin<Project> {
    @Override
    public void apply(final Project project) {
        project.getExtensions().create("modrinth", ModrinthExtension.class, project);

        project.afterEvaluate(evaluatedProject -> {
            final Task mainTask = evaluatedProject.getTasks().create("modrinth", TaskModrinthUpload.class);
            mainTask.setGroup("publishing");
            mainTask.setDescription("Upload project to Modrinth");
            mainTask.mustRunAfter(evaluatedProject.getTasks().getByName("build"));

            TaskModrinthUpload.detectGameVersionForge(evaluatedProject);
            TaskModrinthUpload.detectGameVersionFabric(evaluatedProject);
        });

        project.getLogger().debug("Successfully applied the Modrinth plugin!");
    }
}