package com.modrinth.minotaur.dependencies.container;

import javax.inject.Inject;

import org.gradle.api.NamedDomainObjectContainer;

import com.modrinth.minotaur.dependencies.DependenciesConfiguation;

/**
 * The root DependencyContainer class
 */
public class DependencyContainer {
	
	/**
	 * The Named DependencyObjectContainer reference for nested subclasses 
	 */
	protected final NamedDomainObjectContainer<DependencyObjectContainer> dependencyContainer;
	
	/**
	 * Instantiates a new Dependency object.
	 *
	 * @param deps DependenciesConfiguation
	 */
	@Inject
	public DependencyContainer(DependenciesConfiguation deps) {
		this.dependencyContainer = deps.getDependencies();
	}
	
	/**
	 * Incompatible DependencyType container class
	 */
	public static class Incompatible extends DependencyContainer {

		/**
		 * Instantiates a new incompatible object.
		 *
		 * @param deps DependenciesConfiguation
		 */
		@Inject
		public Incompatible(DependenciesConfiguation deps) {
			super(deps);
		}

		/**
		 * Creates a incompatible Dependency Container and applies the projectId property
		 *
		 * @param projectId the project id
		 */
		public void mod(final String projectId) {
			this.dependencyContainer.register(projectId, mod ->{
				mod.getProjectId().set(projectId);
				mod.incompatible();
			});
		}
		
		/**
		 * Creates a incompatible Dependency Container and applies the versionId property
		 *
		 * @param versionId the version id
		 */
		public void version(final String versionId) {
			this.dependencyContainer.register(versionId, mod ->{
				mod.getVersionId().set(versionId);
				mod.incompatible();
			});
		}
	}
	
	/**
	 * Optional DependencyType container class
	 */
	public static class Optional extends DependencyContainer {

		/**
		 * Instantiates a new optional object.
		 *
		 * @param deps DependenciesConfiguation
		 */
		@Inject
		public Optional(DependenciesConfiguation deps) {
			super(deps);
		}
	
		/**
		 * Creates a optional Dependency Container and applies the projectId property
		 *
		 * @param projectId the project id
		 */
		public void mod(final String projectId) {
			this.dependencyContainer.register(projectId, mod ->{
				mod.getProjectId().set(projectId);
				mod.optional();
			});
		}
		
		/**
		 * Creates a optional Dependency Container and applies the versionId property
		 *
		 * @param versionId the version id
		 */
		public void version(final String versionId) {
			this.dependencyContainer.register(versionId, mod ->{
				mod.getVersionId().set(versionId);
				mod.optional();
			});
		}
	}
	
	/**
	 * Required DependencyType container class
	 */
	public static class Required extends DependencyContainer {
		
		/**
		 * Instantiates a new required object.
		 *
		 * @param deps DependenciesConfiguation
		 */
		@Inject
		public Required(DependenciesConfiguation deps) {
			super(deps);
		}
		
		/**
		 * Creates a required Dependency Container and applies the projectId property
		 *
		 * @param projectId the project id
		 */
		public void mod(final String projectId) {
			this.dependencyContainer.register(projectId, mod ->{
				mod.getProjectId().set(projectId);
				mod.required();
			});
		}
		
		/**
		 * Creates a required Dependency Container and applies the versionId property
		 *
		 * @param versionId the version id
		 */
		public void version(final String versionId) {
			this.dependencyContainer.register(versionId, mod ->{
				mod.getVersionId().set(versionId);
				mod.required();
			});
		}
	}
}
