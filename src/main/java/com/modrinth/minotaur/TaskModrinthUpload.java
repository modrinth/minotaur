package com.modrinth.minotaur;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.modrinth.minotaur.request.RequestData;
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

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * A task used to communicate with Modrinth for the purpose of uploading build artifacts.
 */
public class TaskModrinthUpload extends DefaultTask {
    /**
     * Constant gson instance used for deserializing the API responses when files are uploaded.
     */
    private static final Gson GSON = new GsonBuilder().create();

    /**
     * The extension used for getting the data supplied in the buildscript.
     */
    private final ModrinthExtension extension = getProject().getExtensions().getByType(ModrinthExtension.class);

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

    /**
     * Checks if the upload was successful or not. This is provided as a small helper for use
     * in the build script.
     *
     * @return Whether the file was successfully uploaded.
     */
    public boolean wasUploadSuccessful() {
        return this.uploadInfo != null && this.errorInfo == null;
    }

    @TaskAction
    public void apply() {
        try {
            // Attempt to automatically resolve the game version if one wasn't specified.
            if (extension.getGameVersions().get().isEmpty()) {
                this.detectGameVersionForge();
                this.detectGameVersionFabric();
            }

            if (extension.getGameVersions().get().isEmpty()) {
                throw new GradleException("Can not upload to Modrinth. No game versions specified.");
            }

            if (extension.getLoaders().get().isEmpty()) {
                this.addLoaderForPlugin("net.minecraftforge.gradle", "forge");
                this.addLoaderForPlugin("fabric-loom", "fabric");
            }

            if (extension.getLoaders().get().isEmpty()) {
                throw new GradleException("Unable upload to Modrinth: no loaders specified!");
            }

            // Use project version if no version is specified.
            extension.getVersionNumber().get();

            extension.getVersionName().get();

            // Set a default changelog if the dev hasn't provided one.
            extension.getChangelog().get();

            List<File> filesToUpload = new ArrayList<>();

            final File file = resolveFile(this.getProject(), extension.getUploadFile().get());

            // Ensure the file actually exists before trying to upload it.
            if (file == null || !file.exists()) {
                this.getProject().getLogger().error("The upload file is missing or null. {}", extension.getUploadFile().get());
                throw new GradleException("The upload file is missing or null. " + extension.getUploadFile().get());
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
                final URI endpoint = new URI(this.getUploadEndpoint());
                try {
                    this.upload(endpoint, filesToUpload);
                } catch (final IOException e) {
                    this.getProject().getLogger().error("Failed to upload the file!", e);
                    throw new GradleException("Failed to upload the file!", e);
                }
            } catch (final URISyntaxException e) {
                this.getProject().getLogger().error("Invalid endpoint URI!", e);
                throw new GradleException("Invalid endpoint URI!", e);
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
     * @param endpoint The upload endpoint.
     * @param files    The files to upload.
     * @throws IOException Whenever something goes wrong wit uploading the file.
     */
    public void upload(URI endpoint, List<File> files) throws IOException {
        final HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES).build()).build();
        final HttpPost post = new HttpPost(endpoint);

        post.addHeader("Authorization", extension.getToken().get());

        final MultipartEntityBuilder form = MultipartEntityBuilder.create();

        List<String> fileParts = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            fileParts.add(String.valueOf(i));
        }

        final RequestData data = new RequestData();
        data.setProjectId(extension.getProjectId().get());
        data.setVersionNumber(extension.getVersionNumber().get());
        data.setVersionTitle(extension.getVersionName().get());
        data.setChangelog(extension.getChangelog().get());
        data.setVersionType(extension.getVersionType().get());
        data.setGameVersions(extension.getGameVersions().get());
        data.setLoaders(extension.getLoaders().get());
        data.setDependencies(extension.getDependencies().get());
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
        return extension.getApiUrl().get() + "/version";
    }

    /**
     * Attempts to resolve a file using an arbitrary object provided by a user defined gradle
     * task.
     *
     * @param project  The project instance. This is used as a last resort to resolve the file
     *                 using Gradle's built-in handling.
     * @param in       The arbitrary input object from the user.
     * @return A file handle for the resolved input. If the input can not be resolved this will
     * be null or the fallback.
     */
    @Nullable
    private static File resolveFile(Project project, Object in) {
        // If input or project is null shortcut to the fallback.
        if (in == null || project == null) {
            return null;
        }

        // If the file is a Java file handle no additional handling is needed.
        else if (in instanceof File) {
            return (File) in;
        }

        // Grabs the file from an archive task. Allows build scripts to do things like the jar
        // task directly.
        else if (in instanceof AbstractArchiveTask) {
            return ((AbstractArchiveTask) in).getArchiveFile().get().getAsFile();
        }

        // Fallback to Gradle's built-in file resolution mechanics.
        return project.file(in);
    }

    /**
     * Attempts to detect the game version by detecting ForgeGradle data in the build
     * environment.
     */
    private void detectGameVersionForge() {
        try {
            final ExtraPropertiesExtension extraProps = this.getProject().getExtensions().getExtraProperties();

            // ForgeGradle will store the game version here.
            // https://github.com/MinecraftForge/ForgeGradle/blob/9252ffe1fa5c2acf133f35d169ba4ffc84e6a9fd/src/userdev/java/net/minecraftforge/gradle/userdev/MinecraftUserRepo.java#L179
            if (extraProps.has("MC_VERSION")) {
                //noinspection ConstantConditions
                final String forgeGameVersion = extraProps.get("MC_VERSION").toString();

                if (forgeGameVersion != null && !forgeGameVersion.isEmpty()) {
                    this.getLogger().debug("Detected fallback game version {} from ForgeGradle.", forgeGameVersion);
                    extension.getGameVersions().get().add(forgeGameVersion);
                }
            }
        } catch (final Exception e) {
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
                extension.getGameVersions().get().add(loomGameVersion);
            }
        } catch (final Exception e) {
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
        if (extension.getDetectLoaders().get()) {
            try {
                final AppliedPlugin plugin = this.getProject().getPluginManager().findPlugin(pluginName);

                if (plugin != null) {
                    extension.getLoaders().get().add(loaderName);
                    this.getLogger().debug("Applying loader {} because plugin {} was found.", loaderName, pluginName);
                } else {
                    this.getLogger().debug("Could not automatically apply loader {} because plugin {} has not been applied.", loaderName, pluginName);
                }
            } catch (final Exception e) {
                this.getLogger().debug("Failed to detect plugin {}.", pluginName, e);
            }
        }
    }
}
