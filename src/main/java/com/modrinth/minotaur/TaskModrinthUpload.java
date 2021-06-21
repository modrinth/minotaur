package com.modrinth.minotaur;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import javax.annotation.Nullable;

import com.modrinth.minotaur.request.Dependency;
import com.modrinth.minotaur.request.VersionType;
import com.modrinth.minotaur.responses.ResponseError;
import com.modrinth.minotaur.responses.ResponseUpload;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.plugins.AppliedPlugin;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import com.modrinth.minotaur.request.RequestData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A task used to communicate with Modrinth for the purpose of uploading build artifacts.
 */
public class TaskModrinthUpload extends DefaultTask {
    
    /**
     * Constant gson instance used for deserializing the API responses when files are uploaded.
     */
    private static final Gson GSON = new GsonBuilder().create();
    
    /**
     * The URL used for communicating with Modrinth. This should not be changed unless you know
     * what you're doing. It's main use case is for debug, development, or advanced user
     * configurations.
     */
    public String apiURL = "https://api.modrinth.com/api";
    
    /**
     * The API token used to communicate with Modrinth. Make sure you keep this private!
     */
    public String token;
    
    /**
     * The ID of the project to upload the file to.
     */
    public String projectId;
    
    /**
     * The version of the project being uploaded.
     */
    public String versionNumber;

    /**
     * The version name of the project being uploaded. Defaults to the version number.
     */
    public String versionName;
    
    /**
     * The change log data to associate with the new file.
     */
    public String changelog;
    
    /**
     * The upload artifact file. This can be any object type that is resolvable by
     * {@link #resolveFile(Project, Object, File)}.
     */
    public Object uploadFile;

    public Collection<Object> additionalFiles = new ArrayList<>();
    
    /**
     * The version type for the project.
     */
    public VersionType versionType = VersionType.RELEASE;

    /**
     * The game versions of the game the version supports.
     */
    public Set<String> gameVersions = new HashSet<>();

    /**
     * The mod loaders of the game the version supports.
     */
    public Set<String> loaders = new HashSet<>();

    /**
     * The dependencies of the version.
     */
    public Set<Dependency> dependencies = new HashSet<>();
    
    /**
     * Allows build to continue even if the upload failed.
     */
    public boolean failSilently = false;

    /**
     * If enabled the plugin will try to define loaders based on other plugins in the project
     * environment.
     */
    public boolean detectLoaders = true;
    
    /**
     * The response from the API when the file was uploaded successfully.
     */
    @Nullable
    public ResponseUpload uploadInfo = null;
    
    /**
     * The response from the API when the file failed to upload.
     */
    @Nullable
    public ResponseError errorInfo = null;

    public TaskModrinthUpload() {
        this.mustRunAfter(this.getProject().getTasks().getByName("build"));
    }

    public void addGameVersion (String version) {

        this.getProject().getLogger().debug("Adding game version {} to project {}.", version, this.projectId);

        if (!this.gameVersions.add(version)) {

            this.getProject().getLogger().warn("The game version {} was already applied for project {}.", version, this.projectId);
        }
    }

    public void addLoader (String loader) {

        this.getProject().getLogger().debug("Adding loader tag {} to project {}.", loader, this.projectId);

        if (!this.loaders.add(loader)) {

            this.getProject().getLogger().warn("The loader tag {} was already applied for project {}.", loader, this.projectId);
        }
    }

    /**
     * Adds a dependency to the uploaded version. This determines things like
     * dependencies and incompatibilities.
     *
     * @param versionId The version to add a dependency with.
     * @param type The type of dependency to add.
     */
    public void addDependency(String versionId, Dependency.DependencyType type) {

        this.dependencies.add(new Dependency(versionId, type));
        this.getLogger().debug("Added {} dependency with version ID {}.", type, versionId);
    }

    public void addFile(Object file) {
        additionalFiles.add(file);
    }

    /**
     * Checks if the upload was successful or not. This is provided as a small helper for use
     * in the build script.
     * 
     * @return Whether or not the file was successfully uploaded.
     */
    public boolean wasUploadSuccessful () {
        return this.uploadInfo != null && this.errorInfo == null;
    }
    
    @TaskAction
    public void apply () {
        try {
            // Attempt to automatically resolve the game version if one wasn't specified.
            if (this.gameVersions.isEmpty()) {
                this.detectGameVersionForge();
                this.detectGameVersionFabric();
            }

            if (this.gameVersions.isEmpty()) {
                throw new GradleException("Can not upload to Modrinth. No game versions specified.");
            }

            if(this.loaders.isEmpty()) {
                this.addLoaderForPlugin("net.minecraftforge.gradle", "forge");
                this.addLoaderForPlugin("fabric-loom", "fabric");
            }

            if (this.loaders.isEmpty()) {
                throw new GradleException("Can not upload to Modrinth. No loaders specified.");
            }

            // Use project version if no version is specified.
            if (this.versionNumber == null) {
                this.versionNumber = this.getProject().getVersion().toString();
            }

            if (this.versionName == null) {
                this.versionName = this.getProject().getVersion().toString();
            }
            
            // Set a default changelog if the dev hasn't provided one.
            if (this.changelog == null) {
                this.changelog = "The project has been updated to " + this.versionName + ". No changelog was specified.";
            }

            List<File> filesToUpload = new ArrayList<>();

            final File file = resolveFile(this.getProject(), this.uploadFile, null);
            
            // Ensure the file actually exists before trying to upload it.
            if (file == null || !file.exists()) {
                this.getProject().getLogger().error("The upload file is missing or null. {}", this.uploadFile);
                throw new GradleException("The upload file is missing or null. " + String.valueOf(this.uploadFile));
            }

            filesToUpload.add(file);

            for (Object fileObject : this.additionalFiles)  {
                final File resolvedFile = resolveFile(this.getProject(), fileObject, null);

                // Ensure the file actually exists before trying to upload it.
                if (resolvedFile == null || !resolvedFile.exists()) {

                    this.getProject().getLogger().error("The upload file is missing or null. {}", fileObject);
                    throw new GradleException("The upload file is missing or null. " + String.valueOf(fileObject));
                }

                filesToUpload.add(resolvedFile);
            }
            
            try {
                
                final URI endpoint = new URI(this.getUploadEndpoint());
                try {
                    this.upload(endpoint, filesToUpload);
                }
                catch (final IOException e) {
                    this.getProject().getLogger().error("Failed to upload the file!", e);
                    throw new GradleException("Failed to upload the file!", e);
                }
            }
            
            catch (final URISyntaxException e) {
                this.getProject().getLogger().error("Invalid endpoint URI!", e);
                throw new GradleException("Invalid endpoint URI!", e);
            }
        }
        
        catch (final Exception e) {
            if (this.failSilently) {
                this.getLogger().info("Failed to upload to Modrinth. Check logs for more info.");
                this.getLogger().error("Modrinth upload failed silently.", e);
            }
            else {
                throw e;
            }
        }
    }
    
    /**
     * Uploads a file using the provided configuration.
     * 
     * @param endpoint The upload endpoint.
     * @param files The files to upload.
     * @throws IOException Whenever something goes wrong wit uploading the file.
     */
    public void upload (URI endpoint, List<File> files) throws IOException {
        final HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES).build()).build();
        final HttpPost post = new HttpPost(endpoint);
        
        post.addHeader("Authorization", this.token);
        
        final MultipartEntityBuilder form = MultipartEntityBuilder.create();

        List<String> fileParts = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            fileParts.add(String.valueOf(i));
        }
        
        final RequestData data = new RequestData();
        data.setProjectId(this.projectId);
        data.setVersionNumber(this.versionNumber);
        data.setVersionTitle(this.versionName);
        data.setChangelog(this.changelog);
        data.setVersionType(this.versionType);
        data.setGameVersions(this.gameVersions);
        data.setLoaders(this.loaders);
        data.setDependencies(this.dependencies);
        data.setFileParts(fileParts);
        
        form.addTextBody("data", GSON.toJson(data), ContentType.APPLICATION_JSON);

        for (int i = 0; i < files.size(); i++) {
            this.getProject().getLogger().debug("Uploading {} to {}.", files.get(i).getPath(), this.getUploadEndpoint());
            form.addBinaryBody(String.valueOf(i), files.get(i));
        }

        post.setEntity(form.build());
        
        try {
            
            final HttpResponse response = client.execute(post);
            final int status = response.getStatusLine().getStatusCode();
            
            if (status == 200) {
                
                this.uploadInfo = GSON.fromJson(EntityUtils.toString(response.getEntity()), ResponseUpload.class);
                this.getProject().getLogger().lifecycle("Successfully uploaded version to {} as version id {}.", this.projectId, this.uploadInfo.getId());
            }
            
            else {
                
                this.errorInfo = GSON.fromJson(EntityUtils.toString(response.getEntity()), ResponseError.class);
                this.getProject().getLogger().error("Upload failed! Status: {} Error: {} Reason: {}", status, this.errorInfo.getError(), this.errorInfo.getDescription());
                throw new GradleException("Upload failed! Status: " + status + " Reason: " + this.errorInfo.getDescription());
            }
        }
        
        catch (final IOException e) {
            
            this.getProject().getLogger().error("Failure to upload files!", e);
            throw e;
        }
    }

    /**
     * Provides the upload API endpoint to use.
     * 
     * @return The upload API endpoint.
     */
    private String getUploadEndpoint () {
        
        return this.apiURL + "/v1/version";
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
     * Attempts to detect the game version by detecting ForgeGradle data in the build
     * environment.
     */
    private void detectGameVersionForge () {

        try {

            final ExtraPropertiesExtension extraProps = this.getProject().getExtensions().getExtraProperties();

            // ForgeGradle will store the game version here.
            // https://github.com/MinecraftForge/ForgeGradle/blob/9252ffe1fa5c2acf133f35d169ba4ffc84e6a9fd/src/userdev/java/net/minecraftforge/gradle/userdev/MinecraftUserRepo.java#L179
            if (extraProps.has("MC_VERSION")) {

                final String forgeGameVersion = extraProps.get("MC_VERSION").toString();

                if (forgeGameVersion != null && !forgeGameVersion.isEmpty()) {

                    this.getLogger().debug("Detected fallback game version {} from ForgeGradle.", forgeGameVersion);
                    this.addGameVersion(forgeGameVersion);
                }
            }
        }

        catch (final Exception e) {

            this.getLogger().debug("Failed to detect ForgeGradle game version.", e);
        }
    }

    /**
     * Attempts to detect the game version by detecting LoomGradle data in the build
     * environment.
     */
    private void detectGameVersionFabric() {
        // Loom/Fabric Gradle detection.
        try {
            // Using reflection because loom isn't always available.
            final Class<?> loomType = Class.forName("net.fabricmc.loom.LoomGradleExtension");
            final Method getProvider = loomType.getMethod("getMinecraftProvider");

            final Class<?> minecraftProvider = Class.forName("net.fabricmc.loom.providers.MinecraftProvider");
            final Method getVersion = minecraftProvider.getMethod("getMinecraftVersion");

            final Object loomExt = this.getProject().getExtensions().getByType(loomType);
            final Object loomProvider = getProvider.invoke(loomExt);
            final Object loomVersion = getVersion.invoke(loomProvider);

            final String loomGameVersion = loomVersion.toString();

            if (loomGameVersion != null && !loomGameVersion.isEmpty()) {

                this.getLogger().debug("Detected fallback game version {} from Loom.", loomGameVersion);
                this.addGameVersion(loomGameVersion);
            }
        }
        catch (final Exception e) {
            this.getLogger().debug("Failed to detect Loom game version.", e);
        }
    }

    /**
     * Applies a mod loader automatically if a plugin with the specified name has been applied.
     *
     * @param pluginName The plugin to search for.
     * @param loaderName The mod loader to apply.
     */
    private void addLoaderForPlugin(String pluginName, String loaderName) {
        if (this.detectLoaders) {
            try {
                final AppliedPlugin plugin = this.getProject().getPluginManager().findPlugin(pluginName);

                if (plugin != null) {

                    this.addLoader(loaderName);
                    this.getLogger().debug("Applying loader {} because plugin {} was found.", loaderName, pluginName);
                }

                else {

                    this.getLogger().debug("Could not automatically apply loader {} because plugin {} has not been applied.", loaderName, pluginName);
                }
            }
            catch (final Exception e) {
                this.getLogger().debug("Failed to detect plugin {}.", pluginName, e);
            }
        }
    }
}
