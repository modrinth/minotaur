package com.modrinth.minotaur.dependencies;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Locale;

/**
 * Represents the superclass for {@link ModDependency} and {@link VersionDependency}.
 */
@SuppressWarnings("unused")
public class Dependency {

    /**
     * The {@link DependencyType} of the dependency.
     */
    @Expose
    @SerializedName("dependency_type")
    private final String dependencyType;

    /**
     * Creates a new dependency relationship.
     *
     * @param id   The ID of the project or version to create a dependency with.
     * @param type The type of dependency being created.
     */
    public Dependency(String id, DependencyType type) {
        this.dependencyType = type.toString().toLowerCase(Locale.ROOT);
    }

    /**
     * Creates a new dependency relationship.
     *
     * @param id   The ID of the project or version to create a dependency with.
     * @param type The type of dependency being created.
     */
    public Dependency(String id, String type) {
        this.dependencyType = type;
    }

    /**
     * @return {@link #dependencyType}
     */
    public String getDependencyType() {
        return this.dependencyType;
    }
}
