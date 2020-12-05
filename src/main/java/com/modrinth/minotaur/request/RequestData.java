package com.modrinth.minotaur.request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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
    private String releaseType;

    @Expose
    @SerializedName("game_versions")
    private Collection<String> gameVersions = new ArrayList<>();

    @Expose
    @SerializedName("loaders")
    private Collection<String> loaders = new ArrayList<>();
    
    @Expose
    @SerializedName("dependencies")
    private Collection<String> dependencies = new ArrayList<>();

    @Expose
    @SerializedName("file_parts")
    private List<String> fileParts = new ArrayList<>();

    @Expose
    @SerializedName("featured")
    private boolean featured = false;
    
    public void setVersionNumber(String version) {
        this.versionNumber = version;
    }

    public void setVersionTitle(String title) {
        this.versionTitle = title;
    }
    
    public void setChangelog(String changelog) {
        this.changelog = changelog;
    }
    
    public void setReleaseType(String releaseType) {
        this.releaseType = releaseType;
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
}
