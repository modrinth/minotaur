package com.modrinth.minotaur.dependencies.container;

import com.modrinth.minotaur.dependencies.DependencyType;
import org.gradle.api.provider.ListProperty;

import javax.inject.Inject;

/**
 * A proxy to a dependency collection that exposes a DSL for adding dependencies by project ID or version ID.
 */
public class NamedDependencyContainer {
	private final ListProperty<NamedDependency> dependencyContainer;
	private final DependencyType dependencyType;

	@Inject
	protected NamedDependencyContainer(ListProperty<NamedDependency> dependencyContainer, DependencyType dependencyType) {
		this.dependencyContainer = dependencyContainer;
		this.dependencyType = dependencyType;
	}

	/**
	 * Adds dependencies to this container by project ID
	 *
	 * @param projectIds the project id(s)
	 */
	public void project(final String... projectIds) {
		for (String projectId : projectIds) {
			this.dependencyContainer.add(new NamedDependency(projectId, null, this.dependencyType));
		}
	}

	/**
	 * Adds dependencies to this container by version ID
	 *
	 * @param versionIds the version id(s)
	 */
	public void version(final String... versionIds) {
		for (String versionId : versionIds) {
			this.dependencyContainer.add(new NamedDependency(null, versionId, this.dependencyType));
		}
	}

	/**
	 * Adds a dependency to this container by project ID and version ID
	 *
	 * @param projectId the project id
	 * @param versionId the version number
	 */
	public void version(final String projectId, final String versionId) {
		this.dependencyContainer.add(new NamedDependency(projectId, versionId, this.dependencyType));
	}

	public static class Incompatible extends NamedDependencyContainer {
		@Inject
		public Incompatible(ListProperty<NamedDependency> container) {
			super(container, DependencyType.INCOMPATIBLE);
		}
	}

	public static class Optional extends NamedDependencyContainer {
		@Inject
		public Optional(ListProperty<NamedDependency> container) {
			super(container, DependencyType.OPTIONAL);
		}
	}

	public static class Required extends NamedDependencyContainer {
		@Inject
		public Required(ListProperty<NamedDependency> container) {
			super(container, DependencyType.REQUIRED);
		}
	}

	public static class Embedded extends NamedDependencyContainer {
		@Inject
		public Embedded(ListProperty<NamedDependency> container) {
			super(container, DependencyType.EMBEDDED);
		}
	}
}
