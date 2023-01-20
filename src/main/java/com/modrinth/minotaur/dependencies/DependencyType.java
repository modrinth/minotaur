package com.modrinth.minotaur.dependencies;

import com.google.gson.annotations.SerializedName;
import masecla.modrinth4j.model.version.ProjectVersion.ProjectDependencyType;

import java.util.Locale;

/**
 * The enum representing the three types of dependencies supported by Modrinth.
 */
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
    INCOMPATIBLE,

    /**
     * The version contains this other project/version within it.
     */
    @SerializedName("embedded")
    EMBEDDED;

    public String toString() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    /**
     * Convert this old form to the new fancy form
     */
    public ProjectDependencyType toNew() {
        return ProjectDependencyType.valueOf(this.name().toUpperCase(Locale.ROOT));
    }
}
