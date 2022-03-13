package com.modrinth.minotaur.request;

import com.google.gson.annotations.SerializedName;

/**
 * The enum representing the three version types available for versions.
 */
@SuppressWarnings("unused")
public enum VersionType {
    /**
     * The version is in a stable state.
     */
    @SerializedName("release")
    RELEASE,
    /**
     * The version is in an experimental state which may have bugs.
     */
    @SerializedName("beta")
    BETA,
    /**
     * The version is in a volatile state which could result in data loss.
     */
    @SerializedName("alpha")
    ALPHA
}
