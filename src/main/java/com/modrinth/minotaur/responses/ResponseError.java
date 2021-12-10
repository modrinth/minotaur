package com.modrinth.minotaur.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResponseError {
    @Expose
    @SerializedName("error")
    private String error;

    @Expose
    @SerializedName("description")
    private String description;

    public String getError() {
        return this.error;
    }

    public String getDescription() {
        return this.description;
    }
}