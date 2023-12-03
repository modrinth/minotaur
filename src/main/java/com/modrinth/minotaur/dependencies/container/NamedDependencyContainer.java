package com.modrinth.minotaur.dependencies.container;

import com.modrinth.minotaur.dependencies.DependencyType;
import org.gradle.api.NamedDomainObjectContainer;

import javax.inject.Inject;

/**
 * The root NamedDependencyContainer class
 */
public class NamedDependencyContainer {
	private final NamedDomainObjectContainer<NamedDependency> dependencyContainer;
	private final DependencyType dependencyType;

	/**
	 * Instantiates a new Dependency object.
	 *
	 * @param container      {@literal NamedDomainObjectContainer<NamedDependency>}
	 * @param dependencyType {@link DependencyType}
	 */
	@Inject
	protected NamedDependencyContainer(NamedDomainObjectContainer<NamedDependency> container, DependencyType dependencyType) {
		this.dependencyContainer = container;
		this.dependencyType = dependencyType;
	}

	/**
	 * Creates an incompatible Dependency Container and applies the projectId property
	 *
	 * @param projectIds the project id(s)
	 */
	public void project(final String... projectIds) {
		for (String projectId : projectIds)
			this.dependencyContainer.add(new NamedDependency(projectId, null, this.dependencyType));
	}

	/**
	 * Creates a incompatible Dependency Container and applies the versionId property
	 *
	 * @param versionIds the version id(s)
	 */
	public void version(final String... versionIds) {
		for (String versionId : versionIds)
			this.dependencyContainer.add(new NamedDependency(null, versionId, this.dependencyType));
	}

	/**
	 * Creates a incompatible Dependency Container and applies the versionId property
	 *
	 * @param projectId the project id
	 * @param versionId the version number
	 */
	public void version(final String projectId, final String versionId) {
		this.dependencyContainer.add(new NamedDependency(projectId, versionId, this.dependencyType));
	}

	/**
	 * Incompatible DependencyType container class
	 */
	public static class Incompatible extends NamedDependencyContainer {
		/**
		 * Instantiates a new incompatible object.
		 *
		 * @param container {@literal NamedDomainObjectContainer<NamedDependency>}
		 */
		@Inject
		public Incompatible(NamedDomainObjectContainer<NamedDependency> container) {
			super(container, DependencyType.INCOMPATIBLE);
		}
	}

	/**
	 * Optional DependencyType container class
	 */
	public static class Optional extends NamedDependencyContainer {
		/**
		 * Instantiates a new optional object.
		 *
		 * @param container {@literal NamedDomainObjectContainer<NamedDependency>}
		 */
		@Inject
		public Optional(NamedDomainObjectContainer<NamedDependency> container) {
			super(container, DependencyType.OPTIONAL);
		}
	}

	/**
	 * Required DependencyType container class
	 */
	public static class Required extends NamedDependencyContainer {
		/**
		 * Instantiates a new required object.
		 *
		 * @param container {@literal NamedDomainObjectContainer<NamedDependency>}
		 */
		@Inject
		public Required(NamedDomainObjectContainer<NamedDependency> container) {
			super(container, DependencyType.REQUIRED);
		}
	}

	/**
	 * Embedded DependencyType container class
	 */
	public static class Embedded extends NamedDependencyContainer {
		/**
		 * Instantiates a new required object.
		 *
		 * @param container {@literal NamedDomainObjectContainer<NamedDependency>}
		 */
		@Inject
		public Embedded(NamedDomainObjectContainer<NamedDependency> container) {
			super(container, DependencyType.EMBEDDED);
		}
	}
}
