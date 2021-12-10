package com.modrinth.minotaur;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class Minotaur implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getLogger().debug("Successfully applied the Modrinth plugin!");
    }
}