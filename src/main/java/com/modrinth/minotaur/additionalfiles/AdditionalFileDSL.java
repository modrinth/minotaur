package com.modrinth.minotaur.additionalfiles;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;

import javax.inject.Inject;

/**
 * the Nested AdditionalFiles configuration
 */
public abstract class AdditionalFileDSL {
	@Inject
	protected abstract ObjectFactory getObjects();

	/**
	 * @return additional files to be uploaded alongside the main file
	 */
	public abstract ListProperty<TypedFileCollection> getAdditionalFiles();

	/**
	 * Creates a required resource pack AdditionalFile Container
	 *
	 * @param file the file
	 */
	public void requiredResourcePack(final Object file) {
		addTyped(AdditionalFileType.REQUIRED_RESOURCE_PACK, file);
	}

	/**
	 * Creates an optional resource pack AdditionalFile Container
	 *
	 * @param file the file
	 */
	public void optionalResourcePack(final Object file) {
		addTyped(AdditionalFileType.OPTIONAL_RESOURCE_PACK, file);
	}

	/**
	 * Creates a sources JAR AdditionalFile Container
	 *
	 * @param file the file
	 */
	public void sourcesJar(final Object file) {
		addTyped(AdditionalFileType.SOURCES_JAR, file);
	}

	/**
	 * Creates a dev JAR AdditionalFile Container
	 *
	 * @param file the file
	 */
	public void devJar(final Object file) {
		addTyped(AdditionalFileType.DEV_JAR, file);
	}

	/**
	 * Creates a Javadoc JAR AdditionalFile Container
	 *
	 * @param file the file
	 */
	public void javadocJar(final Object file) {
		addTyped(AdditionalFileType.JAVADOC_JAR, file);
	}

	/**
	 * Creates a signature AdditionalFile Container
	 *
	 * @param file the file
	 */
	public void signature(final Object file) {
		addTyped(AdditionalFileType.SIGNATURE, file);
	}

	/**
	 * Creates another AdditionalFile Container
	 *
	 * @param file the file
	 */
	public void other(final Object file) {
		addTyped(AdditionalFileType.OTHER, file);
	}

	private void addTyped(AdditionalFileType type, Object file) {
		TypedFileCollection namedFiles = getObjects().newInstance(TypedFileCollection.class);
		namedFiles.getType().set(type);
		namedFiles.getFiles().from(file);
		this.getAdditionalFiles().add(namedFiles);
	}
}
