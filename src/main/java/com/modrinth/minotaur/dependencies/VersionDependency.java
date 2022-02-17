package com.modrinth.minotaur.dependencies;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Class for making a dependency on a specific version.
 */
@SuppressWarnings("unused")
public class VersionDependency extends Dependency {

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
        super(id, type);
        this.versionId = id;
    }

    /**
     * Creates a new version relationship.
     *
     * @param id   The ID of the version to create a dependency with.
     * @param type The type of dependency being created.
     */
    public VersionDependency(String id, String type) {
        super(id, type);
        this.versionId = id;
    }

    /**
     * @return {@link #versionId}
     */
    public String getVersionId() {
        return this.versionId;
    }
}
