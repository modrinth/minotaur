package com.modrinth.minotaur;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.modrinth.minotaur.compat.FabricLoomCompatibility;
import com.modrinth.minotaur.dependencies.Dependency;
import com.modrinth.minotaur.request.VersionData;
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
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A task used to communicate with Modrinth for the purpose of uploading build artifacts.
 */
public class TaskModrinthUpload extends DefaultTask {
    /**
     * Constant gson instance used for deserializing the API responses.
     */
    private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * The extension used for getting the data supplied in the buildscript.
     */
    private final ModrinthExtension extension = getProject().getExtensions().getByType(ModrinthExtension.class);

    /**
     * The response from the API when the file was uploaded successfully. Provided as a utility for those manually
     * creating their upload task.
     */
    @Nullable
    public ResponseUpload uploadInfo = null;

    /**
     * The response from the API when the file failed to upload. Provided as a utility for those manually creating their
     * upload task.
     */
    @Nullable
    public ResponseError errorInfo = null;
    
    /**
     * Internal initially empty List to add Dependencies to if any were added
     */
    private final List<Dependency> dependencies = new ArrayList<>();

    /**
     * Checks if the upload was successful or not. Provided as a utility for those manually creating their upload task.
     *
     * @return Whether the file was successfully uploaded.
     */
    @SuppressWarnings("unused")
    public boolean wasUploadSuccessful() {
        return this.uploadInfo != null && this.errorInfo == null;
    }

    /**
     * Defines what to do when the Modrinth upload task is invoked.
     * <ol>
     *   <li>Attempts to automatically resolve various metadata items if not specified, throwing an exception if some
     *   things still don't have anything set</li>
     *   <li>Resolves each file or task to be uploaded, ensuring they're all valid</li>
     *   <li>Uploads these files to the Modrinth API under a new version</li>
     * </ol>
     * This is all in a try/catch block so that, if {@link ModrinthExtension#getFailSilently()} is enabled, it won't
     * fail the build if it fails to upload the version to Modrinth.
     */
    @TaskAction
    public void apply() {
        this.getLogger().lifecycle("Minotaur: {}", this.getClass().getPackage().getImplementationVersion());
        try {
            // Add version number if it's null
            if (extension.getVersionNumber().getOrNull() == null) {
                extension.getVersionNumber().set(this.getProject().getVersion().toString());
            }

            // Add version name if it's null
            if (extension.getVersionName().getOrNull() == null) {
                extension.getVersionName().set(extension.getVersionNumber().get());
            }

            // Attempt to automatically resolve the game version if one wasn't specified.
            if (extension.getGameVersions().get().isEmpty()) {
                this.detectGameVersionForge();
                this.detectGameVersionFabric();
            }

            if (extension.getGameVersions().get().isEmpty()) {
                throw new GradleException("Cannot upload to Modrinth: no game versions specified!");
            }

            if (extension.getLoaders().get().isEmpty() && extension.getDetectLoaders().get()) {
                this.addLoaderForPlugin("net.minecraftforge.gradle", "forge");
                this.addLoaderForPlugin("fabric-loom", "fabric");
                this.addLoaderForPlugin("org.quiltmc.loom", "quilt");
            }

            if (extension.getLoaders().get().isEmpty()) {
                throw new GradleException("Cannot upload to Modrinth: no loaders specified!");
            }
            
            // Retrieves each DependencyContainer if any and adds a new Dependency based on if the projectId property is set 
            this.dependencies.addAll(extension.getNamedDependenciesAsList());
            this.dependencies.addAll(extension.getDependencies().get());

            List<File> filesToUpload = new ArrayList<>();

            final Object primaryFile = extension.getUploadFile().get();
            final File file = resolveFile(this.getProject(), primaryFile);

            // Ensure the file actually exists before trying to upload it.
            if (file == null || !file.exists()) {
                this.getProject().getLogger().error("The upload file is missing or null. {}", primaryFile);
                throw new GradleException("The upload file is missing or null. " + primaryFile);
            }

            filesToUpload.add(file);

            for (Object fileObject : extension.getAdditionalFiles().get()) {
                final File resolvedFile = resolveFile(this.getProject(), fileObject);

                // Ensure the file actually exists before trying to upload it.
                if (resolvedFile == null || !resolvedFile.exists()) {
                    this.getProject().getLogger().error("The upload file is missing or null. {}", fileObject);
                    throw new GradleException("The upload file is missing or null. " + fileObject);
                }

                filesToUpload.add(resolvedFile);
            }

            try {
                this.upload(filesToUpload);
            } catch (final IOException e) {
                this.getProject().getLogger().error("Failed to upload the file!", e);
                throw new GradleException("Failed to upload the file!", e);
            }
        } catch (final Exception e) {
            if (extension.getFailSilently().get()) {
                this.getLogger().info("Failed to upload to Modrinth. Check logs for more info.");
                this.getLogger().error("Modrinth upload failed silently.", e);
            } else {
                throw e;
            }
        }
    }

    /**
     * Uploads a file using the provided configuration.
     *
     * @param files    The files to upload.
     * @throws IOException Whenever something goes wrong wit uploading the file.
     */
    public void upload(List<File> files) throws IOException {
        final HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES).build()).build();
        final HttpPost post = new HttpPost(this.getUploadEndpoint());

        post.addHeader("Authorization", extension.getToken().get());

        final MultipartEntityBuilder form = MultipartEntityBuilder.create();

        List<String> fileParts = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            fileParts.add(String.valueOf(i));
        }

        final VersionData data = new VersionData();
        data.setProjectId(extension.getProjectId().get());
        data.setVersionNumber(extension.getVersionNumber().get());
        data.setVersionTitle(extension.getVersionName().get());
        data.setChangelog(extension.getChangelog().get());
        data.setVersionType(extension.getVersionType().get().toLowerCase(Locale.ROOT));
        data.setGameVersions(extension.getGameVersions().get());
        data.setLoaders(extension.getLoaders().get());
        data.setDependencies(this.dependencies);
        data.setFileParts(fileParts);
        data.setPrimaryFile("0"); // The primary file will always be of the first index in the list

        if (extension.getDebugMode().get()) {
            this.getProject().getLogger().lifecycle("Full data to be sent for upload: {}", GSON.toJson(data));
            this.getProject().getLogger().lifecycle("Minotaur debug mode is enabled. Not going to upload this version.");
            return;
        }

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
                this.getProject().getLogger().lifecycle("Successfully uploaded version {} to {} as version ID {}.", this.uploadInfo.getVersionNumber(), extension.getProjectId().get(), this.uploadInfo.getId());
            } else {
                this.errorInfo = GSON.fromJson(EntityUtils.toString(response.getEntity()), ResponseError.class);
                this.getProject().getLogger().error("Upload failed! Status: {} Error: {} Reason: {}", status, this.errorInfo.getError(), this.errorInfo.getDescription());
                throw new GradleException("Upload failed! Status: " + status + " Reason: " + this.errorInfo.getDescription());
            }
        } catch (final IOException e) {
            this.getProject().getLogger().error("Failure to upload files!", e);
            throw e;
        }
    }

    /**
     * Provides the upload API endpoint to use.
     *
     * @return The upload API endpoint.
     */
    private String getUploadEndpoint() {
        String apiUrl = extension.getApiUrl().get();
        return apiUrl + (apiUrl.endsWith("/") ? "" : "/") + "version";
    }

    /**
     * Attempts to resolve a file using an arbitrary object provided by a user defined gradle
     * task.
     *
     * @param project  The project instance. This is used as a last resort to resolve the file using Gradle's built-in
     *                 handling.
     * @param in       The arbitrary input object from the user.
     * @return A file handle for the resolved input. If the input can not be resolved this will be null or the fallback.
     */
    @Nullable
    private static File resolveFile(Project project, Object in) {
        // If input or project is null we can't really do anything...
        if (in == null || project == null) {
            return null;
        }

        // If the file is a Java file handle no additional handling is needed.
        else if (in instanceof File) {
            return (File) in;
        }

        // Grabs the file from an archive task. Allows build scripts to do things like the jar task directly.
        else if (in instanceof AbstractArchiveTask) {
            return ((AbstractArchiveTask) in).getArchiveFile().get().getAsFile();
        }

        // Grabs the file from an archive task wrapped in a provider. Allows Kotlin DSL buildscripts to also specify
        // the jar task directly, rather than having to call #get() before running.
        else if (in instanceof TaskProvider<?>) {
            Object provided = ((TaskProvider<?>) in).get();

            // Check to see if the task provided is actually an AbstractArchiveTask.
            if (provided instanceof AbstractArchiveTask) {
                return ((AbstractArchiveTask) provided).getArchiveFile().get().getAsFile();
            }
        }

        // Fallback to Gradle's built-in file resolution mechanics.
        return project.file(in);
    }

    /**
     * Attempts to detect the game version by detecting ForgeGradle data in the build environment.
     */
    private void detectGameVersionForge() {
        // TODO confirm this actually works
        Project project = this.getProject();
        ModrinthExtension extension = project.getExtensions().getByType(ModrinthExtension.class);

        try {
            final ExtraPropertiesExtension extraProps = project.getExtensions().getExtraProperties();

            // ForgeGradle will store the game version here.
            // https://github.com/MinecraftForge/ForgeGradle/blob/7ca294b2c1f57be675c11a6164bc2e07a41802f1/src/userdev/java/net/minecraftforge/gradle/userdev/MinecraftUserRepo.java#L199
            if (extraProps.has("MC_VERSION")) {
                //noinspection ConstantConditions
                final String forgeGameVersion = extraProps.get("MC_VERSION").toString();

                if (forgeGameVersion != null && !forgeGameVersion.isEmpty()) {
                    project.getLogger().debug("Detected fallback game version {} from ForgeGradle.", forgeGameVersion);
                    if (extension.getGameVersions().get().isEmpty()) {
                        project.getLogger().debug("Adding game version {} because the game versions list is empty.", forgeGameVersion);
                        extension.getGameVersions().add(forgeGameVersion);
                    }
                }
            }
        } catch (final Exception e) {
            project.getLogger().debug("Failed to detect ForgeGradle game version.", e);
        }
    }

    /**
     * Attempts to detect the game version by detecting Loom data in the build environment.
     */
    private void detectGameVersionFabric() {
        Project project = this.getProject();
        ModrinthExtension extension = this.getProject().getExtensions().getByType(ModrinthExtension.class);

        if (project.getPluginManager().findPlugin("fabric-loom") != null ||
            project.getPluginManager().findPlugin("org.quiltmc.loom") != null) {
            try {
                String loomGameVersion = FabricLoomCompatibility.detectGameVersion(project);
                if (extension.getGameVersions().get().isEmpty()) {
                    project.getLogger().debug("Detected fallback game version {} from Loom.", loomGameVersion);
                    extension.getGameVersions().add(loomGameVersion);
                } else {
                    project.getLogger().debug("Detected fallback game version {} from Loom, but did not apply because game versions list is not empty.", loomGameVersion);
                }
            } catch (final Exception e) {
                project.getLogger().debug("Failed to detect Loom game version.", e);
            }
        } else {
            project.getLogger().debug("Loom is not present; no game versions were added.");
        }
    }

    /**
     * Applies a mod loader automatically if a plugin with the specified name has been applied.
     *
     * @param pluginName The plugin to search for.
     * @param loaderName The mod loader to apply.
     */
    private void addLoaderForPlugin(String pluginName, String loaderName) {
        try {
            final AppliedPlugin plugin = this.getProject().getPluginManager().findPlugin(pluginName);

            if (plugin != null) {
                extension.getLoaders().add(loaderName);
                this.getLogger().debug("Applying loader {} because plugin {} was found.", loaderName, pluginName);
            } else {
                this.getLogger().debug("Could not automatically apply loader {} because plugin {} has not been applied.", loaderName, pluginName);
            }
        } catch (final Exception e) {
            this.getLogger().debug("Failed to detect plugin {}.", pluginName, e);
        }
    }
}
