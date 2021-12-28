package com.modrinth.minotaur;

import com.modrinth.minotaur.request.Dependency;
import com.modrinth.minotaur.request.VersionType;
import org.gradle.api.Project;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

/**
 * Class defining the extension used for configuring {@link TaskModrinthUpload}. This is done via the `modrinth {...}'
 * block in the buildscript.
 */
public class ModrinthExtension {
    private final Property<String> apiUrl, token, projectId, versionNumber, versionName, changelog;
    private final Property<Object> uploadFile;
    private final ListProperty<Object> additionalFiles;
    private final Property<VersionType> versionType;
    private final ListProperty<String> gameVersions, loaders;
    private final ListProperty<Dependency> dependencies;
    private final Property<Boolean> failSilently, detectLoaders;

    public ModrinthExtension(Project project) {
        apiUrl = project.getObjects().property(String.class);
        token = project.getObjects().property(String.class);
        projectId = project.getObjects().property(String.class);
        versionNumber = project.getObjects().property(String.class);
        versionName = project.getObjects().property(String.class);
        changelog = project.getObjects().property(String.class);
        uploadFile = project.getObjects().property(Object.class);
        additionalFiles = project.getObjects().listProperty(Object.class);
        versionType = project.getObjects().property(VersionType.class);
        gameVersions = project.getObjects().listProperty(String.class);
        loaders = project.getObjects().listProperty(String.class);
        dependencies = project.getObjects().listProperty(Dependency.class);
        failSilently = project.getObjects().property(Boolean.class);
        detectLoaders = project.getObjects().property(Boolean.class);
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
     * {@link TaskModrinthUpload#resolveFile(Project, Object)}.
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
    public Property<VersionType> getVersionType() {
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
}
