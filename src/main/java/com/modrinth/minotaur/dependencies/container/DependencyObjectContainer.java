package com.modrinth.minotaur.dependencies.container;

import javax.inject.Inject;

import org.gradle.api.Named;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import com.modrinth.minotaur.dependencies.DependencyType;

/**
 * The Dependency container class to create a NamedDomainObjectContainer against
 */
public class DependencyObjectContainer implements Named {

    private final String name;
    private final Property<String> projectId, versionId, type;
    
	
    /**
     * Instantiates a new dependency container.
     *
     * @param name the DependencyContainer name
     * @param factory the ObjectFactory that is injected by Gradle
     */
    @Inject
    public DependencyObjectContainer(final String name, final ObjectFactory factory) {
        this.name = name;
        this.type = factory.property(String.class);
        this.projectId = factory.property(String.class);
        this.versionId = factory.property(String.class);
    }

    /**
     * 
     * @return the container name, is either projectId or versionId
     */
    @Override
    public String getName() {
        return this.name;
    }
    
    /**
     * Used by an internal check to determine if we add a ModDependency
     *
     * @return the project id
     */
    public Property<String> getProjectId() {
        return this.projectId;
    }
    
    /**
     * Used by an internal check to determine if we add a VersionDependency
     *
     * @return the version id
     */
    public Property<String> getVersionId() {
        return this.versionId;
    }

    /**
     * Gets the DependencyType as String.
     *
     * @return the type
     */
    public Property<String> getType() {
        return this.type;
    }
    
    /**
     * Used to set the dependencyType in {@link RequiredObject}
     */
    void required() {
    	this.type.set(DependencyType.REQUIRED.toString().toLowerCase());
    }

    /**
     * Used to set the dependencyType in {@link OptionalObject}
     */
    void optional() {
        this.type.set(DependencyType.OPTIONAL.toString().toLowerCase());
    }
    
    /**
     * Used to set the dependencyType in {@link IncompatibleObject}
     */
    void incompatible() {
    	this.type.set(DependencyType.INCOMPATIBLE.toString().toLowerCase());
    }
}
