package com.modrinth.minotaur.dependencies.container;

import com.modrinth.minotaur.dependencies.Dependency;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.model.ObjectFactory;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * the Nested Dependencies configuration
 */
public class DependencyDSL {

    private NamedDomainObjectContainer<NamedDependency> dependencies;
	// allows us to reference the objects in our dependencies closure inside the modrinth block
    private NamedDependencyContainer.Incompatible incompatible;
    private NamedDependencyContainer.Optional optional;
    private NamedDependencyContainer.Required required;

    /**
     * Instantiates a new dependencies configuration.
     *
     * @param objects ObjectFactory
     */
    @Inject
    protected DependencyDSL(final ObjectFactory objects) {
        this.dependencies = objects.domainObjectContainer(NamedDependency.class);
        this.incompatible = objects.newInstance(NamedDependencyContainer.Incompatible.class, dependencies);
        this.optional = objects.newInstance(NamedDependencyContainer.Optional.class, dependencies);
        this.required = objects.newInstance(NamedDependencyContainer.Required.class, dependencies);
    }

    /**
     * Returns the complete NamedDependency container set mapped and collected as a {@literal List<Dependency>}
     * 
     * @return {@literal List<Dependency>}
     */
    public List<Dependency> getNamedDependenciesAsList() {
        return this.dependencies.stream().map(NamedDependency::getDependency).collect(Collectors.toList());
    }

	/**
     * Retrieve the reference to an {@link NamedDependencyContainer.Incompatible} instance.
     * Provided as a utility method for external uses.
     * 
     * @return incompatible {@link NamedDependencyContainer.Incompatible}
     */
	public NamedDependencyContainer.Incompatible getIncompatible() {
		return this.incompatible;
	}

	/**
     * Retrieve the reference to an {@link NamedDependencyContainer.Optional} instance.
     * Provided as a utility method for external uses.
     * 
     * @return optional {@link NamedDependencyContainer.Optional}
     */
	public NamedDependencyContainer.Optional getOptional() {
		return this.optional;
	}

	/**
     * Retrieve the reference to an {@link NamedDependencyContainer.Required} instance.
     * Provided as a utility method for external uses.
     * 
     * @return required {@link NamedDependencyContainer.Required}
     */
	public NamedDependencyContainer.Required getRequired() {
		return this.required;
	}
}
