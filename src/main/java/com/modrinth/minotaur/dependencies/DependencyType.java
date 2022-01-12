package com.modrinth.minotaur.dependencies;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public enum DependencyType {
    /**
     * The version requires an instance of the project/version to work.
     */
    @SerializedName("required")
    REQUIRED,

    /**
     * The version has additional functionality when the project/version is present.
     */
    @SerializedName("optional")
    OPTIONAL,

    /**
     * The version is not compatible with the project/version and will not work when both are used.
     */
    @SerializedName("incompatible")
    INCOMPATIBLE
}
