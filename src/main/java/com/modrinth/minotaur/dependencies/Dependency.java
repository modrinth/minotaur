package com.modrinth.minotaur.dependencies;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.ApiStatus;

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
    private final DependencyType dependencyType;

    /**
     * Creates a new dependency relationship.
     *
     * @param type The type of dependency being created.
     */
    @ApiStatus.Internal
    Dependency(String type) {
        this.dependencyType = DependencyType.fromString(type);
    }

    /**
     * Creates a new dependency relationship.
     *
     * @param type The type of dependency being created.
     */
    @ApiStatus.Internal
    Dependency(DependencyType type) {
        this.dependencyType = type;
    }

    /**
     * @return {@link #dependencyType}
     */
    public String getDependencyType() {
        return this.dependencyType.toString();
    }
}
