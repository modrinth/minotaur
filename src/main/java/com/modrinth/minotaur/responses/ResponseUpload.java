package com.modrinth.minotaur.responses;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResponseUpload {
    @Expose
    @SerializedName("id")
    private String id;

    public String getId () {
        
        return this.id;
    }
}