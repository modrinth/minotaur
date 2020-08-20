package com.diluv.diluvgradle;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import com.diluv.diluvgradle.responses.ResponseUpload;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A task used to communicate with Diluv for the purpose of uploading build artifacts.
 */
public class TaskDiluvUpload extends DefaultTask {
            
    private static final Gson GSON = new GsonBuilder().create();
    
    private static final Pattern SEM_VER = Pattern.compile("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");
    
    /**
     * The URL used for communicating with Diluv. This should not be changed unless you know
     * what you're doing. It's main use case is for debug, development, or advanced user
     * configurations.
     */
    public String apiURL = "https://api.diluv.com";
    
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
    
    @Nullable
    private ResponseUpload uploadInfo;
    
    @Nullable
    public ResponseUpload getUploadInfo() {
        
        return this.uploadInfo;
    }
    
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
        
        // Attempt to automatically resolve the game version if one wasn't specified.
        if (this.gameVersion == null) {
            
            this.gameVersion = detectGameVersion(this.getProject());
        }
        
        // Use project version if no version is specified.
        if (this.projectVersion == null) {
        	
        	this.projectVersion = this.getProject().getVersion().toString();
        }
        
        if (!isSemanticVersion(this.projectVersion)) {
        	
        	this.getProject().getLogger().error("Project version {} is not semantic versioning. The file can not be uploaded. https://semver.org", this.projectVersion);
        	return;
        }
        
        final File file = resolveFile(this.getProject(), this.uploadFile, null);
        this.getProject().getLogger().debug("Uploading {} to {}.", file.getPath(), this.getUploadEndpoint());
        
        final HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES).build()).build();
        final HttpPost post = new HttpPost(new URI(this.getUploadEndpoint()));

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
            
            final HttpResponse response = client.execute(post);
            final int status = response.getStatusLine().getStatusCode();
            
            if (status == 200) {
                
                this.uploadInfo = GSON.fromJson(EntityUtils.toString(response.getEntity()), ResponseUpload.class);
                this.getProject().getLogger().lifecycle("Sucessfully uploaded {} to {} as file id {}.", file.getName(), this.projectId, uploadInfo.getId());
            }
            
            else {
                
                // TODO handle errors better
                this.getProject().getLogger().error("Upload failed! Status: {} Response: {}", status, EntityUtils.toString(response.getEntity()));
            }
        }
        
        catch (IOException e) {

            this.getProject().getLogger().error("Failure to upload file!", e);
            throw e;
        }
    }
    
    
    @Override
    public String toString () {
        
        return "TaskDiluvUpload [apiURL=" + apiURL + ", token=" + (token != null) + ", projectId=" + projectId + ", projectVersion=" + projectVersion + ", changelog=" + changelog + ", uploadFile=" + uploadFile + ", releaseType=" + releaseType + ", classifier=" + classifier + ", gameVersion=" + gameVersion + ", dependencies=" + dependencies + "]";
    }
    
    private String getUploadEndpoint() {
        
        return this.apiURL + "/v1/projects/" + this.projectId + "/files";
    }
    
    private static boolean isSemanticVersion(String version) {
    	
    	return SEM_VER.matcher(version).matches();
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

    /**
     * Attempts to automatically detect a game version based on the script environment. This is intended as a fallback and should never override user specified data.
     * @param project The Gradle project to look through.
     * @return A detected game version string. This will be null if nothing was found.
     */
    @Nullable
    private static String detectGameVersion(Project project) {
        
        String version = null;
        
        // ForgeGradle will store the game version here.
        // https://github.com/MinecraftForge/ForgeGradle/blob/9252ffe1fa5c2acf133f35d169ba4ffc84e6a9fd/src/userdev/java/net/minecraftforge/gradle/userdev/MinecraftUserRepo.java#L179
        if (project.getExtensions().getExtraProperties().has("MC_VERSION")) {
            
            version = project.getExtensions().getExtraProperties().get("MC_VERSION").toString();
        }
        
        // TODO Add loom support.
        
        project.getLogger().debug("Using fallback game version {}.", version);
        return version;
    }
}