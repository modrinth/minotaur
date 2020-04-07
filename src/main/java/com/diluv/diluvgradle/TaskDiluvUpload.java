package com.diluv.diluvgradle;

import java.io.File;

import javax.annotation.Nullable;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

/**
 * A task used to communicate with Diluv for the purpose of uploading build artifacts.
 */
public class TaskDiluvUpload extends DefaultTask {
    
    /**
     * The URL used for communicating with Diluv. This should not be changed unless you know
     * what you're doing. It's main use case is for debug, development, or advanced user
     * configurations.
     */
    private String apiURL = "https://api.diluv.com/";
    
    /**
     * The API token used to communicate with Diluv. Make sure you keep this private!
     */
    private String token;
    
    /**
     * The ID of the project to upload the file to.
     */
    private int projectId;
    
    /**
     * The upload artifact file. This can be any object type that is resolvable by
     * {@link #resolveFile(Project, Object, File)}.
     */
    private Object uploadFile;
    
    /**
     * The change log data to associate with the new file.
     */
    private String changelog;
    
    @TaskAction
    public void apply () {
        
        System.out.println("API: " + this.apiURL);
        System.out.println("Token: " + this.token);
        System.out.println("Project ID: " + this.projectId);
        System.out.println("File file: " + resolveFile(this.getProject(), this.uploadFile, null));
        System.out.println("Changelog: " + this.changelog);
    }
    
    /**
     * Attempts to resolve a file using an arbitrary object provided by a user defined gradle
     * task.
     * 
     * @param project The project instance. This is used as a last resort to resolve the file
     *        using Gradle's built in handling.
     * @param in The arbitrary input object from the user.
     * @param fallback A fallback file to use. This can be null.
     * @return A file handle for the resolved input. If the input can not be resolved this will
     *         be null or the fallback.
     */
    @Nullable
    private static File resolveFile (Project project, Object in, @Nullable File fallback) {
        
        // If input or project is null shortcut to the fallback.
        if (in == null || project == null) {
            
            return fallback;
        }
        
        // If the file is a Java file handle no additional handling is needed.
        else if (in instanceof File) {
            
            return (File) in;
        }
        
        // Grabs the file from an archive task. Allows build scripts to do things like the jar
        // task directly.
        else if (in instanceof AbstractArchiveTask) {
            
            return ((AbstractArchiveTask) in).getArchivePath();
        }
        
        // Fallback to Gradle's built in file resolution mechanics.
        return project.file(in);
    }
}