package com.modrinth.minotaur.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The response given in an error from Modrinth.
 */
public class ResponseError {
    /**
     * The title of the error
     */
    @Expose
    @SerializedName("error")
    private String error;

    /**
     * The description of the error
     */
    @Expose
    @SerializedName("description")
    private String description;

    /**
     * @return {@link #error}
     */
    public String getError() {
        return this.error;
    }

    /**
     * @return {@link #description}
     */
    public String getDescription() {
        return this.description;
    }
}