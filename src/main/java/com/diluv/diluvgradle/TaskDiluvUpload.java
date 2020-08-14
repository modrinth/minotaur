package com.diluv.diluvgradle;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.Nullable;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
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
    public String apiURL = "https://api.diluv.com/v1/projects/files";
    
    /**
     * The API token used to communicate with Diluv. Make sure you keep this public!
     */
    public String token;
    
    /**
     * The ID of the project to upload the file to.
     */
    public int projectId;
    
    /**
     * The version of the project being uploaded.
     */
    public String projectVersion;
    
    /**
     * The change log data to associate with the new file.
     */
    // TODO create a beter default message.
    public String changelog = "The file has been updated.";
    
    /**
     * The upload artifact file. This can be any object type that is resolvable by
     * {@link #resolveFile(Project, Object, File)}.
     */
    public Object uploadFile;

    /**
     * The release type for the project.
     */
    // TODO get a list of valid types
    public String releaseType = "alpha";
    
    /**
     * The type of file being uploaded.
     */
    public String classifier = "binary";
    
    /**
     * The version of the game the file supports.
     */
    // TODO validate this.
    public String gameVersion;
    
    // TODO how to format this
    public String dependencies;
    
    @TaskAction
    public void apply () {
        
        try {
            
            this.upload();
        }
        
        catch (URISyntaxException | IOException e) {
            
            // TODO handle errors better. Build failure?
            this.getProject().getLogger().error("Failure to upload file!", e);
        }
    }
    
    public void upload() throws URISyntaxException, IOException {
        
        final File file = resolveFile(this.getProject(), this.uploadFile, null);
        this.getProject().getLogger().debug("Uploading {} to {}.", file.getPath(), this.apiURL);
        
        final HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES).build()).build();
        final HttpPost post = new HttpPost(new URI(this.apiURL));

        post.addHeader("Authorization", "Bearer " + this.token);
        
        final MultipartEntityBuilder form = MultipartEntityBuilder.create();
        form.addTextBody("project_id", Long.toString(this.projectId));
        form.addTextBody("version", this.projectVersion);
        form.addTextBody("changelog", this.changelog);
        form.addBinaryBody("file", file);
        form.addTextBody("filename", file.getName());
        form.addTextBody("releaseType", this.releaseType);
        form.addTextBody("classifier", this.classifier);
        form.addTextBody("game_versions", this.gameVersion);
        
        if (this.dependencies != null) {
            
            form.addTextBody("dependencies", this.dependencies);
        }
        
        post.setEntity(form.build());
        
        try {
            
            // TODO Expose response data to the script.
            // TODO Check response and post success/fail lifecycle message.
            final HttpResponse response = client.execute(post);
                        
            this.getProject().getLogger().info("Sucessfully uploaded {} to {}.", file.getName(), this.projectId);
        }
        
        catch (IOException e) {

            this.getProject().getLogger().error("Failure to upload file!", e);
            throw e;
        }
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

    @Override
    public String toString () {
        
        return "TaskDiluvUpload [apiURL=" + apiURL + ", token=" + (token != null) + ", projectId=" + projectId + ", projectVersion=" + projectVersion + ", changelog=" + changelog + ", uploadFile=" + uploadFile + ", releaseType=" + releaseType + ", classifier=" + classifier + ", gameVersion=" + gameVersion + ", dependencies=" + dependencies + "]";
    }
}