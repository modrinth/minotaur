package com.modrinth.minotaur.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class Dependency {
    /**
     * The ID of the project to create a dependency with. Will only be null when {@link Dependency#versionId} is not
     * null.
     */
    @Nullable
    @Expose
    @SerializedName("project_id")
    private final String projectId;

    /**
     * The ID of the version to create a dependency with. Will only be null when {@link Dependency#projectId} is not
     * null.
     */
    @Nullable
    @Expose
    @SerializedName("version_id")
    private final String versionId;

    /**
     * The {@link DependencyType} of the dependency.
     */
    @Expose
    @SerializedName("dependency_type")
    private final DependencyType dependencyType;

    /**
     * Creates a new project relationship.
     *
     * @param id          The ID of the project or version to create a dependency with.
     * @param type        The type of dependency being created.
     * @param isVersionId Whether `id` is a version ID or not. Should only ever return true or null (in the case of
     *                    a project ID, this param can be omitted).
     */
    public Dependency(String id, DependencyType type, @Nullable boolean isVersionId) {
        if (isVersionId) {
            this.projectId = id;
            this.versionId = null;
        } else {
            this.versionId = id;
            this.projectId = null;
        }
        this.dependencyType = type;
    }

    public String getVersionId() {
        return this.versionId;
    }

    public String getProjectId() {
        return this.projectId;
    }

    public DependencyType getDependencyType() {
        return this.dependencyType;
    }

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
}
