package com.modrinth.minotaur;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.modrinth.minotaur.dependencies.Dependency;
import com.modrinth.minotaur.responses.ResponseUpload;
import masecla.modrinth4j.endpoints.version.CreateVersion.CreateVersionRequest;
import masecla.modrinth4j.main.ModrinthAPI;
import masecla.modrinth4j.model.version.ProjectVersion;
import masecla.modrinth4j.model.version.ProjectVersion.ProjectDependency;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.modrinth.minotaur.Util.*;

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
    private final ModrinthExtension ext = ext(this.getProject());

    /**
     * Easy way to access Gradle's logging.
     */
    private final Logger log = this.getProject().getLogger();

    /**
     * The response from the API when the file was uploaded successfully.
     * @deprecated Please use {@link #newVersion} instead
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Nullable
    @Deprecated
    public ResponseUpload uploadInfo = null;

    /**
     * The response from the API when the file was uploaded successfully.
     */
    @Nullable
    public ProjectVersion newVersion = null;

    /**
     * Internal initially empty List to add Dependencies to if any were added
     */
    private final List<ProjectDependency> dependencies = new ArrayList<>();

    /**
     * Checks if the upload was successful or not.
     *
     * @return Whether the file was successfully uploaded.
     * @deprecated This check should be done manually
     */
    @SuppressWarnings("unused")
    @Deprecated
    public boolean wasUploadSuccessful() {
        return this.newVersion != null;
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
            ModrinthAPI api = api(this.getProject());

            // Add version number if it's null
            if (ext.getVersionNumber().getOrNull() == null) {
                ext.getVersionNumber().set(this.getProject().getVersion().toString());
            }

            // Add version name if it's null
            if (ext.getVersionName().getOrNull() == null) {
                ext.getVersionName().set(ext.getVersionNumber().get());
            }

            // Attempt to automatically resolve the game version if one wasn't specified.
            if (ext.getGameVersions().get().isEmpty()) {
                this.detectGameVersionForge();
                this.detectGameVersionFabric();
            }

            if (ext.getGameVersions().get().isEmpty()) {
                throw new GradleException("Cannot upload to Modrinth: no game versions specified!");
            }

            if (ext.getLoaders().get().isEmpty() && ext.getDetectLoaders().get()) {
                this.addLoaderForPlugin("net.minecraftforge.gradle", "forge");
                this.addLoaderForPlugin("fabric-loom", "fabric");
                this.addLoaderForPlugin("org.quiltmc.loom", "quilt");
                this.addLoaderForPlugin("org.spongepowered.gradle.plugin", "sponge");
                this.addLoaderForPlugin("io.papermc.paperweight.userdev", "paper");
            }

            if (ext.getLoaders().get().isEmpty()) {
                throw new GradleException("Cannot upload to Modrinth: no loaders specified!");
            }
            
            // Resolve dependencies
            List<Dependency> dependencies = new ArrayList<>();
            dependencies.addAll(ext.getNamedDependenciesAsList());
            dependencies.addAll(ext.getDependencies().get());
            dependencies.stream()
                .map(dependency -> dependency.toNew(this.getProject(), api, ext))
                .forEach(this.dependencies::add);

            List<Object> fileObjects = new ArrayList<>();
            List<File> filesToUpload = new ArrayList<>();
            fileObjects.add(resolveFile(this.getProject(), ext.getUploadFile().get()));
            fileObjects.addAll(ext.getAdditionalFiles().get());

            fileObjects.forEach(file -> {
                final File resolvedFile = resolveFile(this.getProject(), file);

                // Ensure the file actually exists before trying to upload it.
                if (resolvedFile == null || !resolvedFile.exists()) {
                    log.error("The upload file is missing or null. {}", file);
                    throw new GradleException("The upload file is missing or null. " + file);
                }

                filesToUpload.add(resolvedFile);
            });

            this.upload(filesToUpload, api);
        } catch (final Exception e) {
            if (ext.getFailSilently().get()) {
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
     * @param files The files to upload.
     * @param api   {@link ModrinthAPI} instance
     */
    public void upload(List<File> files, ModrinthAPI api) {
        String id = ext.getProjectId().get();
        CreateVersionRequest data = CreateVersionRequest.builder()
            .projectId(api.projects().getProjectIdBySlug(id).join())
            .versionNumber(ext.getVersionNumber().get())
            .name(ext.getVersionName().get())
            .changelog(ext.getChangelog().get().replaceAll("\r\n", "\n"))
            .versionType(ProjectVersion.VersionType.valueOf(ext.getVersionType().get().toUpperCase(Locale.ROOT)))
            .gameVersions(ext.getGameVersions().get().toArray(new String[0]))
            .loaders(ext.getLoaders().get().toArray(new String[0]))
            .dependencies(this.dependencies.toArray(new ProjectDependency[0]))
            .files(files.toArray(new File[0]))
            .build();

        if (ext.getDebugMode().get()) {
            log.lifecycle("Full data to be sent for upload: {}", GSON.toJson(data));
            log.lifecycle("Minotaur debug mode is enabled. Not going to upload this version.");
            return;
        }

        ProjectVersion version = api.versions().createProjectVersion(data).join();
        this.newVersion = version;
        //noinspection deprecation
        this.uploadInfo = new ResponseUpload(version);

        String versionNumber = this.newVersion.getVersionNumber();
        String url = "";
        if (ext.getApiUrl().get().equals(ModrinthExtension.DEFAULT_API_URL)) {
            url = String.format("https://modrinth.com/mod/%s/version/%s", id, versionNumber);
        } else if (ext.getApiUrl().get().equals(ModrinthExtension.STAGING_API_URL)) {
            url = String.format("https://staging.modrinth.com/mod/%s/version/%s", id, versionNumber);
        }

        log.lifecycle(
            "Successfully uploaded version {} to {} as version ID {}. {}",
            versionNumber,
            id,
            this.newVersion.getId(),
            url
        );
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
                if (this.ext.getGameVersions().get().isEmpty()) {
                    log.debug("Adding game version {} because the game versions list is empty.", forgeGameVersion);
                    this.ext.getGameVersions().add(forgeGameVersion);
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
            if (this.ext.getGameVersions().get().isEmpty()) {
                log.debug("Detected fallback game version {} from Loom.", loomGameVersion);
                this.ext.getGameVersions().add(loomGameVersion);
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
            ext.getLoaders().add(loaderName);
            log.debug("Applying loader {} because plugin {} was found.", loaderName, pluginName);
        } else {
            log.debug("Could not automatically apply loader {} because plugin {} has not been applied.", loaderName, pluginName);
        }
    }
}
