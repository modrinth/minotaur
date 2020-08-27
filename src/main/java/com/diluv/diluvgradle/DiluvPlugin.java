package com.diluv.diluvgradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class DiluvPlugin implements Plugin<Project> {
    
    @Override
    public void apply (Project project) {
        
        project.getLogger().debug("Successfully applied the Diluv plugin. Make sure you're using the upload task.");
    }
}