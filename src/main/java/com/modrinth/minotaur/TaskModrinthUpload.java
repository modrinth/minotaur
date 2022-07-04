package com.modrinth.minotaur;

import com.google.gson.Gson;
import com.modrinth.minotaur.dependencies.Dependency;
import com.modrinth.minotaur.dependencies.ModDependency;
import com.modrinth.minotaur.request.VersionData;
import com.modrinth.minotaur.responses.ResponseError;
import com.modrinth.minotaur.responses.ResponseUpload;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.AppliedPlugin;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.tasks.TaskAction;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.modrinth.minotaur.Util.createHttpClient;
import static com.modrinth.minotaur.Util.getExtension;
import static com.modrinth.minotaur.Util.getUploadEndpoint;
import static com.modrinth.minotaur.Util.resolveId;

/**
 * A task used to communicate with Modrinth for the purpose of uploading build artifacts.
 */
public class TaskModrinthUpload extends DefaultTask {
    /**
     * Constant gson instance used for deserializing the API responses.
     */
    private final Gson GSON = Util.createGsonInstance();

    /**
     * The extension used for getting the data supplied in the buildscript.
     */
    private final ModrinthExtension extension = getExtension();

    /**
     * Easy way to access Gradle's logging.
     */
    private final Logger log = this.getProject().getLogger();

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
     * Internal initially empty List to add Dependencies to if any were added
     */
    private final List<Dependency> dependencies = new ArrayList<>();

    /**
     * Checks if the upload was successful or not.
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
        log.lifecycle("Minotaur: {}", this.getClass().getPackage().getImplementationVersion());
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
            
            // Resolve dependencies
            List<Dependency> dependencies = new ArrayList<>();
            dependencies.addAll(extension.getNamedDependenciesAsList());
            dependencies.addAll(extension.getDependencies().get());
            for (Dependency dependency : dependencies) {
                if (dependency instanceof ModDependency) {
                    String id = resolveId(((ModDependency) dependency).getProjectId(), log);
                    this.dependencies.add(new ModDependency(id, dependency.getDependencyType()));
                } else {
                    this.dependencies.add(dependency);
                }
            }

            List<Object> fileObjects = new ArrayList<>();
            List<File> filesToUpload = new ArrayList<>();
            fileObjects.add(Util.resolveFile(extension.getUploadFile().get()));
            fileObjects.addAll(extension.getAdditionalFiles().get());

            for (Object fileObject : fileObjects) {
                final File resolvedFile = Util.resolveFile(fileObject);

                // Ensure the file actually exists before trying to upload it.
                if (resolvedFile == null || !resolvedFile.exists()) {
                    log.error("The upload file is missing or null. {}", fileObject);
                    throw new GradleException("The upload file is missing or null. " + fileObject);
                }

                filesToUpload.add(resolvedFile);
            }

            this.upload(filesToUpload);
        } catch (final Exception e) {
            if (extension.getFailSilently().get()) {
                log.info("Failed to upload to Modrinth. Check logs for more info.");
                log.error("Modrinth upload failed silently.", e);
            } else {
                throw new GradleException("Failed to upload file to Modrinth! " + e.getMessage(), e);
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
        final HttpClient client = createHttpClient();
        final HttpPost post = new HttpPost(getUploadEndpoint() + "version");

        post.addHeader("Authorization", extension.getToken().get());

        final MultipartEntityBuilder form = MultipartEntityBuilder.create();

        List<String> fileParts = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            fileParts.add(String.valueOf(i));
        }

        final VersionData data = new VersionData();
        data.setProjectId(resolveId(extension.getProjectId().get()));
        data.setVersionNumber(extension.getVersionNumber().get());
        data.setVersionTitle(extension.getVersionName().get());
        data.setChangelog(extension.getChangelog().get().replaceAll("\r\n", "\n"));
        data.setVersionType(extension.getVersionType().get().toLowerCase(Locale.ROOT));
        data.setGameVersions(extension.getGameVersions().get());
        data.setLoaders(extension.getLoaders().get());
        data.setDependencies(this.dependencies);
        data.setFileParts(fileParts);
        data.setPrimaryFile("0"); // The primary file will always be of the first index in the list

        if (extension.getDebugMode().get()) {
            log.lifecycle("Full data to be sent for upload: {}", GSON.toJson(data));
            log.lifecycle("Minotaur debug mode is enabled. Not going to upload this version.");
            return;
        }

        form.addTextBody("data", GSON.toJson(data), ContentType.APPLICATION_JSON);

        for (int i = 0; i < files.size(); i++) {
            log.debug("Uploading {} to {}.", files.get(i).getPath(), getUploadEndpoint() + "version");
            form.addBinaryBody(String.valueOf(i), files.get(i));
        }

        post.setEntity(form.build());

        final HttpResponse response = client.execute(post);
        final int status = response.getStatusLine().getStatusCode();

        if (status == 200) {
            this.uploadInfo = GSON.fromJson(EntityUtils.toString(response.getEntity()), ResponseUpload.class);
            assert this.uploadInfo.getVersionNumber() != null;

            String url = "";
            if (extension.getApiUrl().get().equals(ModrinthExtension.DEFAULT_API_URL)) {
                url = String.format("https://modrinth.com/mod/%s/version/%s", extension.getProjectId().get(), this.uploadInfo.getVersionNumber());
            } else if (extension.getApiUrl().get().equals(ModrinthExtension.STAGING_API_URL)) {
                url = String.format("https://staging.modrinth.com/mod/%s/version/%s", extension.getProjectId().get(), this.uploadInfo.getVersionNumber());
            }

            log.lifecycle(
                "Successfully uploaded version {} to {} as version ID {}. {}",
                this.uploadInfo.getVersionNumber(),
                extension.getProjectId().get(),
                this.uploadInfo.getId(),
                url
            );
        } else {
            this.errorInfo = GSON.fromJson(EntityUtils.toString(response.getEntity()), ResponseError.class);
            String error = String.format("Upload failed! Status: %s Error: %s Reason: %s", status, this.errorInfo.getError(), this.errorInfo.getDescription());
            log.error(error);
            throw new GradleException(error);
        }
    }

    /**
     * Attempts to detect the game version by detecting ForgeGradle data in the build environment.
     */
    private void detectGameVersionForge() {
        final ExtraPropertiesExtension extraProps = this.getProject().getExtensions().getExtraProperties();

        // ForgeGradle will store the game version here.
        // https://github.com/MinecraftForge/ForgeGradle/blob/7ca294b2c1f57be675c11a6164bc2e07a41802f1/src/userdev/java/net/minecraftforge/gradle/userdev/MinecraftUserRepo.java#L199
        if (extraProps.has("MC_VERSION")) {
            //noinspection ConstantConditions
            final String forgeGameVersion = extraProps.get("MC_VERSION").toString();

            if (forgeGameVersion != null && !forgeGameVersion.isEmpty()) {
                log.debug("Detected fallback game version {} from ForgeGradle.", forgeGameVersion);
                if (this.extension.getGameVersions().get().isEmpty()) {
                    log.debug("Adding game version {} because the game versions list is empty.", forgeGameVersion);
                    this.extension.getGameVersions().add(forgeGameVersion);
                }
            }
        }
    }

    /**
     * Attempts to detect the game version by detecting Loom data in the build environment.
     */
    private void detectGameVersionFabric() {
        final PluginManager pluginManager = this.getProject().getPluginManager();
        final ConfigurationContainer configurations = this.getProject().getConfigurations();

        if (pluginManager.findPlugin("fabric-loom") != null || pluginManager.findPlugin("org.quiltmc.loom") != null) {
            // Use the same method Loom uses to get the version.
            // https://github.com/FabricMC/fabric-loom/blob/9b2b857b38b4157b5ae468469c5d3bd6ef9fee35/src/main/java/net/fabricmc/loom/configuration/DependencyInfo.java#L60
            final String loomGameVersion = configurations.getByName("minecraft").getDependencies().iterator().next().getVersion();
            assert loomGameVersion != null;
            if (this.extension.getGameVersions().get().isEmpty()) {
                log.debug("Detected fallback game version {} from Loom.", loomGameVersion);
                this.extension.getGameVersions().add(loomGameVersion);
            } else {
                log.debug("Detected fallback game version {} from Loom, but did not apply because game versions list is not empty.", loomGameVersion);
            }
        } else {
            log.debug("Loom is not present; no game versions were added.");
        }
    }

    /**
     * Applies a mod loader automatically if a plugin with the specified name has been applied.
     *
     * @param pluginName The plugin to search for.
     * @param loaderName The mod loader to apply.
     */
    private void addLoaderForPlugin(String pluginName, String loaderName) {
        final AppliedPlugin plugin = this.getProject().getPluginManager().findPlugin(pluginName);

        if (plugin != null) {
            extension.getLoaders().add(loaderName);
            log.debug("Applying loader {} because plugin {} was found.", loaderName, pluginName);
        } else {
            log.debug("Could not automatically apply loader {} because plugin {} has not been applied.", loaderName, pluginName);
        }
    }
}
