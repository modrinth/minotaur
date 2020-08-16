package com.diluv.diluvgradle.responses;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResponseUpload {

    @Expose
    @SerializedName("status")
    private String status;
    
    @Expose
    @SerializedName("lastStatusChanged")
    private Long lastStatusChanged;
    
    @Expose
    @SerializedName("id")
    private Long id;
    
    @Expose
    @SerializedName("name")
    private String name;
    
    @Expose
    @SerializedName("downloadURL")
    private String downloadURL;
    
    @Expose
    @SerializedName("size")
    private Long size;
    
    @Expose
    @SerializedName("changelog")
    private String changelog;
    
    @Expose
    @SerializedName("sha512")
    private String sha512;
    
    @Expose
    @SerializedName("releaseType")
    private String releaseType;
    
    @Expose
    @SerializedName("classifier")
    private String classifier;
    
    @Expose
    @SerializedName("createdAt")
    private Long createdAt;
    
    @Expose
    @SerializedName("dependencies")
    // TODO Is this String or Long
    private List<Object> dependencies = null;
    
    @Expose
    @SerializedName("gameVersions")
    // TODO Is this String or Long
    private List<Object> gameVersions = null;
    
    @Expose
    @SerializedName("gameSlug")
    private String gameSlug;
    
    @Expose
    @SerializedName("projectTypeSlug")
    private String projectTypeSlug;
    
    @Expose
    @SerializedName("projectSlug")
    private String projectSlug;
    
    @Expose
    @SerializedName("uploaderUserId")
    private Long uploaderUserId;
    
    @Expose
    @SerializedName("uploaderUsername")
    private String uploaderUsername;
    
    @Expose
    @SerializedName("uploaderDisplayName")
    private String uploaderDisplayName;

    public String getStatus() {
        return status;
    }

    public Long getLastStatusChanged() {
        return lastStatusChanged;
    }

    public Long getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }

    public String getDownloadURL() {
        return downloadURL;
    }

    public Long getSize() {
        return size;
    }

    public String getChangelog() {
        return changelog;
    }

    public String getSha512() {
        return sha512;
    }

    public String getReleaseType() {
        return releaseType;
    }

    public String getClassifier() {
        return classifier;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public List<Object> getDependencies() {
        return dependencies;
    }

    public List<Object> getGameVersions() {
        return gameVersions;
    }

    public String getGameSlug() {
        return gameSlug;
    }

    public String getProjectTypeSlug() {
        return projectTypeSlug;
    }

    public String getProjectSlug() {
        return projectSlug;
    }

    public Long getUploaderUserId() {
        return uploaderUserId;
    }

    public String getUploaderUsername() {
        return uploaderUsername;
    }
    
    public String getUploaderDisplayName() {
        return uploaderDisplayName;
    }
}