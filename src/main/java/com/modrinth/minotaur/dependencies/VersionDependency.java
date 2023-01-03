package com.modrinth.minotaur.dependencies;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Locale;

/**
 * Class for making a dependency on a specific version.
 */
@SuppressWarnings("unused")
public class VersionDependency extends Dependency {
    /**
     * The ID of the version to create a dependency with.
     */
    @Expose
    @SerializedName("project_id")
    private final String projectId;

    /**
     * The ID of the version to create a dependency with.
     */
    @Expose
    @SerializedName("version_id")
    private final String versionId;

    /**
     * Creates a new version relationship.
     *
     * @param id   The ID of the version to create a dependency with.
     * @param type The type of dependency being created.
     */
    public VersionDependency(String id, DependencyType type) {
        super(type);
        this.projectId = null;
        this.versionId = id;
    }

    /**
     * Creates a new version relationship.
     *
     * @param id   The ID of the version to create a dependency with.
     * @param type The type of dependency being created.
     */
    public VersionDependency(String id, String type) {
        super(type);
        this.projectId = null;
        this.versionId = id;
    }

    /**
     * Creates a new version relationship.
     *
     * @param projectId The ID of the project to create a dependency with.
     * @param versionId The ID of the version to create a dependency with.
     * @param type      The type of dependency being created.
     */
    public VersionDependency(String projectId, String versionId, DependencyType type) {
        super(type);
        this.projectId = projectId;
        this.versionId = versionId;
    }

    /**
     * Creates a new version relationship.
     *
     * @param projectId The ID of the project to create a dependency with.
     * @param versionId The ID of the version to create a dependency with.
     * @param type      The type of dependency being created.
     */
    public VersionDependency(String projectId, String versionId, String type) {
        super(type);
        this.projectId = projectId;
        this.versionId = versionId;
    }

    /**
     * @return {@link #projectId}
     */
    public String getProjectId() {
        return this.projectId;
    }

    /**
     * @return {@link #versionId}
     */
    public String getVersionId() {
        return this.versionId;
    }
}
