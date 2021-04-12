package com.modrinth.minotaur.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Dependency {
    @Expose
    @SerializedName("version_id")
    private final String versionId;

    @Expose
    @SerializedName("dependency_type")
    private final DependencyType dependencyType;

    /**
     * Creates a new project relationship.
     *
     * @param versionId The ID of the project version to create a dependency with.
     * @param dependencyType The type of dependency being created.
     */
    public Dependency(String versionId, DependencyType dependencyType) {

        this.versionId = versionId;
        this.dependencyType = dependencyType;
    }

    public String getVersionId() {

        return this.versionId;
    }

    public DependencyType getDependencyType () {

        return this.dependencyType;
    }

    public enum DependencyType {
        /**
         * The version requires an instance of the project to work.
         */
        @SerializedName("required")
        REQUIRED,

        /**
         * The version has additional functionality when the project is present.
         */
        @SerializedName("optional")
        OPTIONAL,

        /**
         * The version is not compatible with the project and will not work when both are used.
         */
        @SerializedName("incompatible")
        INCOMPATIBLE;
    }
}
