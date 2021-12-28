package com.modrinth.minotaur;

import com.modrinth.minotaur.request.Dependency;
import com.modrinth.minotaur.request.VersionType;
import org.gradle.api.Project;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

// TODO Add javadocs!
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

    public Property<String> getApiUrl() {
        return this.apiUrl;
    }

    public Property<String> getToken() {
        return this.token;
    }

    public Property<String> getProjectId() {
        return this.projectId;
    }

    public Property<String> getVersionNumber() {
        return this.versionNumber;
    }

    public Property<String> getVersionName() {
        return this.versionName;
    }

    public Property<String> getChangelog() {
        return this.changelog;
    }

    public Property<Object> getUploadFile() {
        return this.uploadFile;
    }

    public ListProperty<Object> getAdditionalFiles() {
        return this.additionalFiles;
    }

    public Property<VersionType> getVersionType() {
        return this.versionType;
    }

    public ListProperty<String> getGameVersions() {
        return this.gameVersions;
    }

    public ListProperty<String> getLoaders() {
        return this.loaders;
    }

    public ListProperty<Dependency> getDependencies() {
        return this.dependencies;
    }

    public Property<Boolean> getFailSilently() {
        return this.failSilently;
    }

    public Property<Boolean> getDetectLoaders() {
        return this.detectLoaders;
    }
}
