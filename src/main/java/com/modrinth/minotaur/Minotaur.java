package com.modrinth.minotaur;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

public class Minotaur implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getExtensions().create("modrinth", ModrinthExtension.class, project);

        final Task mainTask = project.getTasks().create("modrinth", TaskModrinthUpload.class);
        mainTask.setGroup("publishing");
        mainTask.setDescription("Upload project to Modrinth");

        project.getLogger().debug("Successfully applied the Modrinth plugin!");
    }
}