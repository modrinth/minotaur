package com.diluv.diluvgradle.request;

import com.google.gson.annotations.Expose;

public class FileDependency {
    @Expose
    public Long projectId;

    @Expose
    public String type;

    public FileDependency(Long projectId, String type) {

        this.projectId = projectId;
        this.type = type;
    }

    public Long getProjectId() {
        return this.projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
