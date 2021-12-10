package com.modrinth.minotaur.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RequestData {
    @Expose
    @SerializedName("mod_id")
    private String projectId;

    @Expose
    @SerializedName("version_number")
    private String versionNumber;

    @Expose
    @SerializedName("version_title")
    private String versionTitle;

    @Expose
    @SerializedName("version_body")
    private String changelog;

    @Expose
    @SerializedName("release_channel")
    private VersionType versionType;

    @Expose
    @SerializedName("game_versions")
    private Collection<String> gameVersions = new ArrayList<>();

    @Expose
    @SerializedName("loaders")
    private Collection<String> loaders = new ArrayList<>();

    @Expose
    @SerializedName("dependencies")
    private Collection<Dependency> dependencies = new ArrayList<>();

    @Expose
    @SerializedName("file_parts")
    private List<String> fileParts = new ArrayList<>();

    @Expose
    @SerializedName("featured")
    private boolean featured = false;

    public void setProjectId(String id) {
        this.projectId = id;
    }

    public void setVersionNumber(String version) {
        this.versionNumber = version;
    }

    public void setVersionTitle(String title) {
        this.versionTitle = title;
    }

    public void setChangelog(String changelog) {
        this.changelog = changelog;
    }

    public void setVersionType(VersionType versionType) {
        this.versionType = versionType;
    }

    public void setGameVersions(Collection<String> gameVersions) {
        this.gameVersions = gameVersions;
    }

    public void setLoaders(Collection<String> loaders) {
        this.loaders = loaders;
    }

    public void setFileParts(List<String> fileParts) {
        this.fileParts = fileParts;
    }

    public void setDependencies(Collection<Dependency> dependencies) {
        this.dependencies = dependencies;
    }
}
