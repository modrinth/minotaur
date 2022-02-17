package com.modrinth.minotaur.dependencies;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Class for making a dependency on a mod.
 */
@SuppressWarnings("unused")
public class ModDependency extends Dependency {

    /**
     * The ID of the project to create a dependency with.
     */
    @Expose
    @SerializedName("project_id")
    private final String projectId;

    /**
     * Creates a new project relationship.
     *
     * @param id   The ID of the project to create a dependency with.
     * @param type The type of dependency being created.
     */
    public ModDependency(String id, DependencyType type) {
        super(id, type);
        this.projectId = id;
    }

    /**
     * Creates a new project relationship.
     *
     * @param id   The ID of the project to create a dependency with.
     * @param type The type of dependency being created.
     */
    public ModDependency(String id, String type) {
        super(id, type);
        this.projectId = id;
    }

    /**
     * @return {@link #projectId}
     */
    public String getProjectId() {
        return this.projectId;
    }
}
