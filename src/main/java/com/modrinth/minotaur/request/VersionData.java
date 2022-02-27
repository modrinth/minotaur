package com.modrinth.minotaur.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.modrinth.minotaur.dependencies.Dependency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class for the data sent when creating a new version.
 */
public class VersionData {
    /**
     * Base62 ID of the project to add the version to
     */
    @Expose
    @SerializedName("project_id")
    private String projectId;

    /**
     * Number of the version to be created
     */
    @Expose
    @SerializedName("version_number")
    private String versionNumber;

    /**
     * Title of the version to be created
     */
    @Expose
    @SerializedName("version_title")
    private String versionTitle;

    /**
     * Changelog of the version to be created. Supports Markdown formatting.
     */
    @Expose
    @SerializedName("version_body")
    private String changelog;

    /**
     * The release channel of the version to be created. Is one of {@link VersionType}.
     */
    @Expose
    @SerializedName("release_channel")
    private String versionType;

    /**
     * List of game versions of the version to be created
     */
    @Expose
    @SerializedName("game_versions")
    private Collection<String> gameVersions = new ArrayList<>();

    /**
     * List of loaders of the version to be created
     */
    @Expose
    @SerializedName("loaders")
    private Collection<String> loaders = new ArrayList<>();

    /**
     * {@link Dependency} list of the version to be created
     */
    @Expose
    @SerializedName("dependencies")
    private Collection<Dependency> dependencies = new ArrayList<>();

    /**
     * The files for the version to be created
     */
    @Expose
    @SerializedName("file_parts")
    private List<String> fileParts = new ArrayList<>();

    /**
     * Whether the version to be created will be featured on the sidebar
     */
    @Expose
    @SerializedName("featured")
    private boolean featured = false;

    @Expose
    @SerializedName("primary_file")
    private String primaryFile;

    /**
     * @param id Value to set {@link #projectId} to
     */
    public void setProjectId(String id) {
        this.projectId = id;
    }

    /**
     * @param version Value to set {@link #versionNumber} to
     */
    public void setVersionNumber(String version) {
        this.versionNumber = version;
    }

    /**
     * @param title Value to set {@link #versionTitle} to
     */
    public void setVersionTitle(String title) {
        this.versionTitle = title;
    }

    /**
     * @param changelog Value to set {@link #changelog} to
     */
    public void setChangelog(String changelog) {
        this.changelog = changelog;
    }

    /**
     * @param versionType Value to set {@link #versionType} to
     */
    public void setVersionType(String versionType) {
        this.versionType = versionType;
    }

    /**
     * @param gameVersions Value to set {@link #gameVersions} to
     */
    public void setGameVersions(Collection<String> gameVersions) {
        this.gameVersions = gameVersions;
    }

    /**
     * @param loaders Value to set {@link #loaders} to
     */
    public void setLoaders(Collection<String> loaders) {
        this.loaders = loaders;
    }

    /**
     * @param fileParts Value to set {@link #fileParts} to
     */
    public void setFileParts(List<String> fileParts) {
        this.fileParts = fileParts;
    }

    /**
     * @param dependencies Value to set {@link #dependencies} to
     */
    public void setDependencies(Collection<Dependency> dependencies) {
        this.dependencies = dependencies;
    }

    /**
     * @param primaryFile Value to set {@link #primaryFile} to
     */
    public void setPrimaryFile(String primaryFile) {
        this.primaryFile = primaryFile;
    }
}
