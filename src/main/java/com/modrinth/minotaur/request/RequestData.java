package com.modrinth.minotaur.request;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RequestData {
    
    @Expose
    @SerializedName("version")
    public String version;
    
    @Expose
    @SerializedName("changelog")
    public String changelog;
    
    @Expose
    @SerializedName("releaseType")
    public String releaseType;
    
    @Expose
    @SerializedName("classifier")
    public String classifier;
    
    @Expose
    @SerializedName("gameVersions")
    public List<String> gameVersions = new ArrayList<>();
    
    @Expose
    @SerializedName("dependencies")
    public List<FileDependency> dependencies = new ArrayList<>();
    
    public String getVersion () {
        
        return this.version;
    }
    
    public void setVersion (String version) {
        
        this.version = version;
    }
    
    public String getChangelog () {
        
        return this.changelog;
    }
    
    public void setChangelog (String changelog) {
        
        this.changelog = changelog;
    }
    
    public String getReleaseType () {
        
        return this.releaseType;
    }
    
    public void setReleaseType (String releaseType) {
        
        this.releaseType = releaseType;
    }
    
    public String getClassifier () {
        
        return this.classifier;
    }
    
    public void setClassifier (String classifier) {
        
        this.classifier = classifier;
    }
    
    public List<String> getGameVersions () {
        
        return this.gameVersions;
    }
    
    public void setGameVersions (List<String> gameVersions) {
        
        this.gameVersions = gameVersions;
    }
    
    public List<FileDependency> getDependencies () {
        
        return this.dependencies;
    }
    
    public void setDependencies (List<FileDependency> dependencies) {
        
        this.dependencies = dependencies;
    }
}
