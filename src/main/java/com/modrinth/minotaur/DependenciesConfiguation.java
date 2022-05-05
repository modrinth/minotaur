package com.modrinth.minotaur;

import javax.inject.Inject;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.model.ObjectFactory;

import com.modrinth.minotaur.dependencies.container.DependencyContainer;
import com.modrinth.minotaur.dependencies.container.DependencyObjectContainer;

/**
 * the Nested Dependencies configuration
 */
public class DependenciesConfiguation  {

	private final NamedDomainObjectContainer<DependencyObjectContainer> dependencies;
	// allows us to reference the objects in our dependencies closure inside the modrinth block
	private final DependencyContainer.Incompatible incompatible;
	private final DependencyContainer.Optional optional;
	private final DependencyContainer.Required required;

    /**
     * Instantiates a new dependencies configuration.
     *
     * @param objects ObjectFactory
     */
    @Inject
    public DependenciesConfiguation(final ObjectFactory objects) {
    	this.dependencies = objects.domainObjectContainer(DependencyObjectContainer.class);
    	this.incompatible = objects.newInstance(DependencyContainer.Incompatible.class, this);
    	this.optional = objects.newInstance(DependencyContainer.Optional.class, this);
    	this.required = objects.newInstance(DependencyContainer.Required.class, this);
    }
    
    /**
     * Returns the Dependencies container that will contain all registered dependencies.
     *
     * @return DependencyObjectContainer
     */
    public NamedDomainObjectContainer<DependencyObjectContainer> getDependencies() {
        return this.dependencies;
    }

	/**
	 * Retrieve the reference to an {@link DependencyContainer.Incompatible} instance  
	 * 
	 * @return incompatible {@link DependencyContainer.Incompatible}
	 */
	public DependencyContainer.Incompatible getIncompatible() {
		return this.incompatible;
	}

	/**
	 * Retrieve the reference to an {@link DependencyContainer.Optional} instance  
	 * 
	 * @return optional {@link DependencyContainer.Optional}
	 */
	public DependencyContainer.Optional getOptional() {
		return this.optional;
	}

	/**
	 * Retrieve the reference to an {@link DependencyContainer.Required} instance  
	 * 
	 * @return required {@link DependencyContainer.Required}
	 */
	public DependencyContainer.Required getRequired() {
		return this.required;
	}
}
