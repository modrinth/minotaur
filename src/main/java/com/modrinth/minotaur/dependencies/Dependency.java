package com.modrinth.minotaur.dependencies;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.modrinth.minotaur.ModrinthExtension;
import masecla.modrinth4j.endpoints.version.GetProjectVersions.GetProjectVersionsRequest;
import masecla.modrinth4j.main.ModrinthAPI;
import masecla.modrinth4j.model.version.ProjectVersion;
import masecla.modrinth4j.model.version.ProjectVersion.ProjectDependency;
import masecla.modrinth4j.model.version.ProjectVersion.ProjectDependencyType;
import org.gradle.api.GradleException;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
	ProjectDependencyType getDependencyType() {
		return this.dependencyType;
	}

	/**
	 * @param api {@link ModrinthAPI} instance
	 * @param ext {@link ModrinthExtension} instance
	 * @return a {@link ProjectDependency} instance from a {@link Dependency}
	 */
	public ProjectDependency toNew(ModrinthAPI api, ModrinthExtension ext) {
		if (this instanceof ModDependency) {
			ModDependency dep = (ModDependency) this;
			String id = Objects.requireNonNull(
				api.projects().getProjectIdBySlug(dep.getProjectId()).join(),
				"Failed to resolve dependency project ID: " + dep.getProjectId()
			);
			return new ProjectDependency(null, id, null, dep.getDependencyType());
		} else if (this instanceof VersionDependency) {
			VersionDependency dep = (VersionDependency) this;
			String versionId = resolveVersionId(dep.getProjectId(), dep.getVersionId(), api);
			return new ProjectDependency(versionId, dep.getProjectId(), null, dep.getDependencyType());
		} else {
			throw new GradleException("Dependency was not an instance of ModDependency or VersionDependency!");
		}
	}

	/**
	 * @param newDep the {@link ProjectDependency} to convert to a {@link Dependency}
	 * @return a converted {@link Dependency}
	 */
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
	private String resolveVersionId(String projectId, String versionId, ModrinthAPI api) {
		try {
			ProjectVersion version = api.versions().getVersionByNumber(projectId, versionId).join();
			return version.getId();
		} catch (Exception e) {
			throw new GradleException("Failed to resolve version \"" + versionId + "\"!", e);
		}
	}
}
