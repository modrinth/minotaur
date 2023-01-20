package com.modrinth.minotaur.dependencies;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.modrinth.minotaur.ModrinthExtension;
import masecla.modrinth4j.endpoints.version.GetProjectVersions;
import masecla.modrinth4j.exception.EndpointException;
import masecla.modrinth4j.main.ModrinthAPI;
import masecla.modrinth4j.model.version.ProjectVersion;
import masecla.modrinth4j.model.version.ProjectVersion.ProjectDependency;
import masecla.modrinth4j.model.version.ProjectVersion.ProjectDependencyType;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.util.Locale;

import static com.modrinth.minotaur.Util.*;

/**
 * Represents the superclass for {@link ModDependency} and {@link VersionDependency}.
 */
public class Dependency {

    /**
     * The {@link ProjectDependencyType} of the dependency.
     */
    @Expose
    @SerializedName("dependency_type")
    private final ProjectDependencyType dependencyType;

    /**
     * Creates a new dependency relationship.
     *
     * @param type The type of dependency being created.
     */
    @ApiStatus.Internal
    Dependency(DependencyType type) {
        this.dependencyType = type.toNew();
    }

    /**
     * Creates a new dependency relationship.
     *
     * @param type The type of dependency being created.
     */
    @ApiStatus.Internal
    Dependency(String type) {
        this.dependencyType = ProjectDependencyType.valueOf(type.toUpperCase(Locale.ROOT));
    }

    /**
     * @return {@link #dependencyType}
     */
    public ProjectDependencyType getDependencyType() {
        return this.dependencyType;
    }

    public ProjectDependency toNew(Project project) throws IOException {
        if (this instanceof ModDependency) {
            ModDependency dep = (ModDependency) this;
            String id = resolveId(project, dep.getProjectId());
            return new ProjectDependency(null, id, null, dep.getDependencyType());
        } else if (this instanceof VersionDependency) {
            VersionDependency dep = (VersionDependency) this;
            String versionId = resolveVersionId(project, dep.getProjectId(), dep.getVersionId());
            return new ProjectDependency(versionId, dep.getProjectId(), null, dep.getDependencyType());
        } else {
            throw new GradleException("Dependency was not an instance of ModDependency or VersionDependency!");
        }
    }

    public static Dependency fromNew(ProjectDependency newDep) {
        if (newDep.getVersionId() != null) {
            return new VersionDependency(newDep.getProjectId(), newDep.getVersionId(), newDep.getDependencyType().name());
        } else {
            return new ModDependency(newDep.getProjectId(), newDep.getDependencyType().name());
        }
    }

    /**
     * Returns a project ID from a project ID or slug
     *
     * @param versionId ID or version number of the project to resolve
     * @return ID of the resolved project
     */
    private String resolveVersionId(Project project, String projectId, String versionId) {
        ModrinthAPI api = api(project);
        ModrinthExtension ext = ext(project);

        try {
            // First check to see if the version is simply a version ID. Return it if so.
            ProjectVersion version = api.versions().getVersion(versionId).join();
            return version.getId();
        } catch (EndpointException ignored) {
            // Seems it wasn't a version ID. Try to extract a version number.
            try {
                GetProjectVersions.GetProjectVersionsRequest filter = GetProjectVersions.GetProjectVersionsRequest.builder()
                    .loaders(ext.loaders().toArray(new String[0]))
                    .gameVersions(ext.gameVersions().toArray(new String[0]))
                    .build();
                ProjectVersion[] versions = api.versions().getProjectVersions(projectId, filter).join();

                for (ProjectVersion version : versions) {
                    if (version.getVersionNumber().equals(versionId)) {
                        return version.getId();
                    }
                }
            } catch (EndpointException e) {
                // Project ID doesn't work? Throw!
                e.setError("Failed to resolve versions of project \"" + projectId + "\"! Error received: " + e.getError());
                throw e;
            }
        }

        // Input wasn't a version ID or a version number
        String error = String.format("Failed to resolve version number \"%s\"!", versionId);
        project.getLogger().error(error);
        throw new GradleException(error);
    }
}
