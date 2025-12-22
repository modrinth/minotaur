package com.modrinth.minotaur.additionalfiles.container;

import com.modrinth.minotaur.additionalfiles.AdditionalFile;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * the Nested AdditionalFiles configuration
 */
public class AdditionalFileDSL {
	private final NamedDomainObjectContainer<NamedAdditionalFile> dependencies;
	private final NamedAdditionalFileContainer.RequiredResourcePack requiredResourcePack;
	private final NamedAdditionalFileContainer.OptionalResourcePack optionalResourcePack;
	private final NamedAdditionalFileContainer.SourcesJar sourcesJar;
	private final NamedAdditionalFileContainer.DevJar devJar;
	private final NamedAdditionalFileContainer.JavadocJar javadocJar;
	private final NamedAdditionalFileContainer.Signature signature;
	private final NamedAdditionalFileContainer.Other other;

	/**
	 * Instantiates a new dependencies configuration.
	 *
	 * @param objects ObjectFactory
	 */
	@Inject
	protected AdditionalFileDSL(final ObjectFactory objects) {
		this.dependencies = objects.domainObjectContainer(NamedAdditionalFile.class);
		this.requiredResourcePack = objects.newInstance(NamedAdditionalFileContainer.RequiredResourcePack.class, dependencies);
		this.optionalResourcePack = objects.newInstance(NamedAdditionalFileContainer.OptionalResourcePack.class, dependencies);
		this.sourcesJar = objects.newInstance(NamedAdditionalFileContainer.SourcesJar.class, dependencies);
		this.devJar = objects.newInstance(NamedAdditionalFileContainer.DevJar.class, dependencies);
		this.javadocJar = objects.newInstance(NamedAdditionalFileContainer.JavadocJar.class, dependencies);
		this.signature = objects.newInstance(NamedAdditionalFileContainer.Signature.class, dependencies);
		this.other = objects.newInstance(NamedAdditionalFileContainer.Other.class, dependencies);
	}

	/**
	 * Returns the complete NamedAdditionalFile container set mapped and collected as a {@literal List<AdditionalFile>}
	 *
	 * @return {@literal List<AdditionalFile>}
	 */
	public List<AdditionalFile> getNamedAdditionalFilesAsList() {
		return this.dependencies.stream().map(NamedAdditionalFile::getAdditionalFile).collect(Collectors.toList());
	}

	/**
	 * Retrieve the reference to an {@link NamedAdditionalFileContainer.RequiredResourcePack} instance.
	 * Provided as a utility method for external uses.
	 *
	 * @return incompatible {@link NamedAdditionalFileContainer.RequiredResourcePack}
	 */
	public NamedAdditionalFileContainer.RequiredResourcePack getRequiredResourcePack() {
		return this.requiredResourcePack;
	}

	/**
	 * Retrieve the reference to an {@link NamedAdditionalFileContainer.OptionalResourcePack} instance.
	 * Provided as a utility method for external uses.
	 *
	 * @return optional {@link NamedAdditionalFileContainer.OptionalResourcePack}
	 */
	public NamedAdditionalFileContainer.OptionalResourcePack getOptionalResourcePack() {
		return this.optionalResourcePack;
	}

	/**
	 * Retrieve the reference to an {@link NamedAdditionalFileContainer.SourcesJar} instance.
	 * Provided as a utility method for external uses.
	 *
	 * @return required {@link NamedAdditionalFileContainer.SourcesJar}
	 */
	public NamedAdditionalFileContainer.SourcesJar getSourcesJar() {
		return this.sourcesJar;
	}

	/**
	 * Retrieve the reference to an {@link NamedAdditionalFileContainer.DevJar} instance.
	 * Provided as a utility method for external uses.
	 *
	 * @return embedded {@link NamedAdditionalFileContainer.DevJar}
	 */
	public NamedAdditionalFileContainer.DevJar getDevJar() {
		return this.devJar;
	}

	/**
	 * Retrieve the reference to an {@link NamedAdditionalFileContainer.JavadocJar} instance.
	 * Provided as a utility method for external uses.
	 *
	 * @return embedded {@link NamedAdditionalFileContainer.JavadocJar}
	 */
	public NamedAdditionalFileContainer.JavadocJar getJavadocJar() {
		return this.javadocJar;
	}

	/**
	 * Retrieve the reference to an {@link NamedAdditionalFileContainer.Signature} instance.
	 * Provided as a utility method for external uses.
	 *
	 * @return embedded {@link NamedAdditionalFileContainer.Signature}
	 */
	public NamedAdditionalFileContainer.Signature getSignature() {
		return this.signature;
	}

	/**
	 * Retrieve the reference to an {@link NamedAdditionalFileContainer.Other} instance.
	 * Provided as a utility method for external uses.
	 *
	 * @return embedded {@link NamedAdditionalFileContainer.Other}
	 */
	public NamedAdditionalFileContainer.Other getOther() {
		return this.other;
	}

}
