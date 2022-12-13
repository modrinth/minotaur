package com.modrinth.minotaur;

import com.modrinth.minotaur.dependencies.Dependency;
import com.modrinth.minotaur.dependencies.container.DependencyDSL;
import com.modrinth.minotaur.request.VersionType;
import org.gradle.api.Project;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

/**
 * Class defining the extension used for configuring {@link TaskModrinthUpload}. This is done via the {@code modrinth
 * {...}} block in the buildscript.
 */
public class ModrinthExtension extends DependencyDSL {
    private final Property<String> apiUrl, token, projectId, versionNumber, versionName, changelog, versionType, syncBodyFrom;
    private final Property<Object> uploadFile;
    private final ListProperty<Object> additionalFiles;
    private final ListProperty<String> gameVersions, loaders;
    private final ListProperty<Dependency> dependencies;
    private final Property<Boolean> failSilently, detectLoaders, debugMode, autoAddDependsOn;

    /**
     * The default API URL in use for uploading. Exposed as a fallback utility.
     */
    public static final String DEFAULT_API_URL = "https://api.modrinth.com/v2";
    /**
     * The staging API URL if desired for testing.
     */
    public static final String STAGING_API_URL = "https://staging-api.modrinth.com/v2";
    /**
     * The default token in use for uploading. Exposed as a fallback utility.
     */
    public static final String DEFAULT_TOKEN = System.getenv("MODRINTH_TOKEN");
    /**
     * The default changelog if one was not provided. Exposed as a fallback utility.
     */
    public static final String DEFAULT_CHANGELOG = "No changelog was specified.";
    /**
     * The default release type if one was not provided. Exposed as a fallback utility.
     */
    public static final String DEFAULT_VERSION_TYPE = "release";

    /**
     * @param project The Gradle project that the extension is applied to
     */
    public ModrinthExtension(Project project) {
        super(project.getObjects());
        apiUrl = project.getObjects().property(String.class).convention(DEFAULT_API_URL);
        token = project.getObjects().property(String.class).convention(DEFAULT_TOKEN);
        projectId = project.getObjects().property(String.class);
        versionNumber = project.getObjects().property(String.class);
        versionName = project.getObjects().property(String.class);
        changelog = project.getObjects().property(String.class).convention(DEFAULT_CHANGELOG);
        uploadFile = project.getObjects().property(Object.class);
        additionalFiles = project.getObjects().listProperty(Object.class).empty();
        versionType = project.getObjects().property(String.class).convention(DEFAULT_VERSION_TYPE);
        gameVersions = project.getObjects().listProperty(String.class).empty();
        loaders = project.getObjects().listProperty(String.class).empty();
        dependencies = project.getObjects().listProperty(Dependency.class).empty();
        failSilently = project.getObjects().property(Boolean.class).convention(false);
        detectLoaders = project.getObjects().property(Boolean.class).convention(true);
        debugMode = project.getObjects().property(Boolean.class).convention(false);
        syncBodyFrom = project.getObjects().property(String.class);
        autoAddDependsOn = project.getObjects().property(Boolean.class).convention(true);
    }

    /**
     * This should not be changed unless you know what you're doing. Its main use case is for debug, development, or
     * advanced user configurations.
     * @return The URL used for communicating with Modrinth.
     */
    public Property<String> getApiUrl() {
        return this.apiUrl;
    }

    /**
     * Make sure you keep this private!
     * @return The API token used to communicate with Modrinth.
     */
    public Property<String> getToken() {
        return this.token;
    }

    /**
     * @return The ID of the project to upload the file to.
     */
    public Property<String> getProjectId() {
        return this.projectId;
    }

    /**
     * @return The version number of the project being uploaded.
     */
    public Property<String> getVersionNumber() {
        return this.versionNumber;
    }

    /**
     * @return The version name of the project being uploaded. Defaults to the version number.
     */
    public Property<String> getVersionName() {
        return this.versionName;
    }

    /**
     * @return The version name of the project being uploaded. Defaults to the version number.
     */
    public Property<String> getChangelog() {
        return this.changelog;
    }

    /**
     * @return The upload artifact file. This can be any object type that is resolvable by
     * {@link Util#resolveFile(Project, Object)}.
     */
    public Property<Object> getUploadFile() {
        return this.uploadFile;
    }

    /**
     * @return Any additional files to be uploaded to the new version.
     */
    public ListProperty<Object> getAdditionalFiles() {
        return this.additionalFiles;
    }

    /**
     * @return The version type for the project. See {@link VersionType}.
     */
    public Property<String> getVersionType() {
        return this.versionType;
    }

    /**
     * @return The game versions of the game the version supports.
     */
    public ListProperty<String> getGameVersions() {
        return this.gameVersions;
    }

    /**
     * @return The mod loaders of the game the version supports.
     */
    public ListProperty<String> getLoaders() {
        return this.loaders;
    }

    /**
     * @return The dependencies of the version.
     */
    public ListProperty<Dependency> getDependencies() {
        return this.dependencies;
    }

    /**
     * @return Whether the build should continue even if the upload failed.
     */
    public Property<Boolean> getFailSilently() {
        return this.failSilently;
    }

    /**
     * @return Whether the plugin will try to define loaders based on other plugins in the project environment.
     */
    public Property<Boolean> getDetectLoaders() {
        return this.detectLoaders;
    }

    /**
     * @return Whether the plugin is in debug mode. Debug mode does not actually upload any files.
     */
    public Property<Boolean> getDebugMode() {
        return this.debugMode;
    }

    /**
     * @return The file to sync the project's body description from
     */
    public Property<String> getSyncBodyFrom() {
        return this.syncBodyFrom;
    }

    /**
     * @return Whether to automatically add the `dependsOn` information for upload files
     */
    public Property<Boolean> getAutoAddDependsOn() {
        return autoAddDependsOn;
    }
}
