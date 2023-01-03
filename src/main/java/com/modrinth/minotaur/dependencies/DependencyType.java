package com.modrinth.minotaur.dependencies;

import com.google.gson.annotations.SerializedName;

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
     * @param in string input
     * @return a {@link DependencyType} from a String
     * @throws IllegalStateException when the input is not one of the recognized types
     */
    public static DependencyType fromString(String in) {
        // Java 8 :evaporate:
        if (in.equalsIgnoreCase("required")) {
            return REQUIRED;
        } else if (in.equalsIgnoreCase("optional")) {
            return OPTIONAL;
        } else if (in.equalsIgnoreCase("incompatible")) {
            return INCOMPATIBLE;
        } else if (in.equalsIgnoreCase("embedded")) {
            return EMBEDDED;
        }

        throw new IllegalStateException("Invalid dependency type specified!" +
            "Must be one of `required`, `optional`, `incompatible`, or `embedded`");
    }
}
