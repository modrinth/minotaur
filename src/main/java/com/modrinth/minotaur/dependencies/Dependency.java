package com.modrinth.minotaur.dependencies;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import masecla.modrinth4j.main.ModrinthAPI;
import masecla.modrinth4j.model.version.ProjectVersion;
import masecla.modrinth4j.model.version.ProjectVersion.ProjectDependency;
import masecla.modrinth4j.model.version.ProjectVersion.ProjectDependencyType;
import org.gradle.api.GradleException;
import org.jetbrains.annotations.ApiStatus;

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
	 * @return a {@link ProjectDependency} instance from a {@link Dependency}
	 */
	public ProjectDependency toNew(ModrinthAPI api) {
		if (this instanceof ModDependency) {
			ModDependency dep = (ModDependency) this;
			String id = Objects.requireNonNull(
				api.projects().getProjectIdBySlug(dep.getProjectId()).join(),
				"Failed to resolve dependency project ID: " + dep.getProjectId()
			);
			return new ProjectDependency(null, id, null, dep.getDependencyType());
		} else if (this instanceof VersionDependency) {
			VersionDependency dep = (VersionDependency) this;
			ProjectVersion version;
			try {
				version = dep.getProjectId() == null
					? api.versions().getVersion(dep.getVersionId()).join()
					: api.versions().getVersionByNumber(dep.getProjectId(), dep.getVersionId()).join();
			} catch (Exception e) {
				throw new GradleException("Failed to resolve version \"" + dep.getVersionId() + "\"!", e);
			}

			if (version == null) {
				throw new GradleException(String.format("Failed to resolve version \"%s\"", dep.getVersionId()));
			}

			return new ProjectDependency(version.getId(), version.getProjectId(), null, dep.getDependencyType());
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
}
