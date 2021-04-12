package com.modrinth.minotaur.request;

import com.google.gson.annotations.SerializedName;

public enum VersionType {
    /**
     * The version is in a stable state.
     */
    @SerializedName("release")
    RELEASE,
    /**
     * The version is in a experimental state which may have bugs.
     */
    @SerializedName("beta")
    BETA,
    /**
     * The version is in a volatile state which could result in data loss.
     */
    @SerializedName("alpha")
    ALPHA
}