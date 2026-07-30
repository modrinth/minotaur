package com.modrinth.minotaur.dependencies.container;

import com.modrinth.minotaur.dependencies.Dependency;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * the Nested Dependencies configuration
 */
public class DependencyDSL {
	private final ListProperty<NamedDependency> dependencies;
	private final NamedDependencyContainer.Incompatible incompatible;
	private final NamedDependencyContainer.Optional optional;
	private final NamedDependencyContainer.Required required;
	private final NamedDependencyContainer.Embedded embedded;

	/**
	 * Instantiates a new dependencies configuration.
	 *
	 * @param objects ObjectFactory
	 */
	@Inject
	protected DependencyDSL(final ObjectFactory objects) {
		this.dependencies = objects.listProperty(NamedDependency.class).empty();
		this.incompatible = objects.newInstance(NamedDependencyContainer.Incompatible.class, dependencies);
		this.optional = objects.newInstance(NamedDependencyContainer.Optional.class, dependencies);
		this.required = objects.newInstance(NamedDependencyContainer.Required.class, dependencies);
		this.embedded = objects.newInstance(NamedDependencyContainer.Embedded.class, dependencies);
	}

	/**
	 * Returns the complete NamedDependency container set mapped and collected as a {@literal List<Dependency>}
	 *
	 * @return {@literal List<Dependency>}
	 * @deprecated this forces eager evaluation; use {@link #getNamedDependencies()} instead
	 */
	@Deprecated
	public List<Dependency> getNamedDependenciesAsList() {
		return this.dependencies.get().stream().map(NamedDependency::getDependency).collect(Collectors.toList());
	}

	/**
	 * @return the complete NamedDependency container set
	 */
	public ListProperty<NamedDependency> getNamedDependencies() {
		return this.dependencies;
	}

	/**
	 * @return the dependency container for incompatible dependencies
	 */
	public NamedDependencyContainer.Incompatible getIncompatible() {
		return this.incompatible;
	}

	/**
	 * @return the dependency container for optional dependencies
	 */
	public NamedDependencyContainer.Optional getOptional() {
		return this.optional;
	}

	/**
	 * @return the dependency container for required dependencies
	 */
	public NamedDependencyContainer.Required getRequired() {
		return this.required;
	}

	/**
	 * @return the dependency container for embedded dependencies
	 */
	public NamedDependencyContainer.Embedded getEmbedded() {
		return this.embedded;
	}
}
