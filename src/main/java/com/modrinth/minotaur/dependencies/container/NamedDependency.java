package com.modrinth.minotaur.dependencies.container;

import com.modrinth.minotaur.dependencies.Dependency;
import com.modrinth.minotaur.dependencies.DependencyType;
import com.modrinth.minotaur.dependencies.ModDependency;
import com.modrinth.minotaur.dependencies.VersionDependency;
import org.gradle.api.GradleException;
import org.gradle.api.Named;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Defines a Named Dependency for our DependencyContainer.
 */
public class NamedDependency implements Named {

    private final String id;
    private String projectId;
    private String versionId;
    private DependencyType dependencyType;

    /**
     * Instantiates a new NamedDependency.
     *
     * @param projectId the projectId if not-null
     * @param versionId the versionId if not-null
     * @param dependencyType the DependencyType
     */
    protected NamedDependency(@Nullable String projectId, @Nullable String versionId, DependencyType dependencyType) {
        this.checkAll(projectId, versionId);
        this.id = isNull(projectId) ? versionId : projectId;
        this.dependencyType = dependencyType;
        this.projectId = projectId;
        this.versionId = versionId;
    }

    /**
     * 
     * @return the container id, is only of either projectId or versionId
     */
    @Override
    public String getName() {
        return this.id;
    }

    /**
     * Gets the DependencyType as String.
     *
     * @return the type
     */
    public DependencyType getDependencyType() {
        return this.dependencyType;
    }

    /**
     * Return this as a never null {@link VersionDependency} or {@link ModDependency} depending on 
     * which id is set.
     * 
     * @return {@link Dependency} dynamically created Dependency
     */
    @Nonnull
    public Dependency getDependency() {
        if (isNull(this.projectId)) {
            return new VersionDependency(this.versionId, this.dependencyType);
        }
        return new ModDependency(this.projectId, this.dependencyType);
    }

    private boolean isNull(final String it) {
        return it == null;
    }

    private void checkAll(@Nullable Object obj1, @Nullable Object obj2) {
        if (obj1 == null && obj2 == null) {
            throw new GradleException("Both 'projectId' & 'versionId' cannot be null. At least 1 must be defined");
        }
    }
}
