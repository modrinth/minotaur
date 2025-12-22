package com.modrinth.minotaur.additionalfiles;

import com.modrinth.minotaur.Util;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * the Nested AdditionalFiles configuration
 */
public class AdditionalFileDSL {
	private final Project project;
	private final NamedDomainObjectContainer<NamedAdditionalFile> additionalFiles;

	/**
	 * Instantiates a new additionalFiles configuration.
	 *
	 * @param project Project
	 */
	@Inject
	public AdditionalFileDSL(final Project project) {
		this.project = project;
		this.additionalFiles = project.getObjects().domainObjectContainer(NamedAdditionalFile.class);
	}

	/**
	 * Returns the complete NamedAdditionalFile container set mapped and collected as a {@literal List<AdditionalFile>}
	 *
	 * @return {@literal List<AdditionalFile>}
	 */
	public List<NamedAdditionalFile> getNamedAdditionalFilesAsList() {
		return new ArrayList<>(this.additionalFiles);
	}

	/**
	 * Creates a required resource pack AdditionalFile Container
	 *
	 * @param file the file
	 */
	public void requiredResourcePack(final Object file) {
		this.additionalFiles.add(new NamedAdditionalFile(AdditionalFileType.REQUIRED_RESOURCE_PACK, Util.resolveFileProperty(project, file).get()));
	}

	/**
	 * Creates an optional resource pack AdditionalFile Container
	 *
	 * @param file the file
	 */
	public void optionalResourcePack(final Object file) {
		this.additionalFiles.add(new NamedAdditionalFile(AdditionalFileType.OPTIONAL_RESOURCE_PACK, Util.resolveFileProperty(project, file).get()));
	}

	/**
	 * Creates a sources JAR AdditionalFile Container
	 *
	 * @param file the file
	 */
	public void sourceJar(final Object file) {
		this.additionalFiles.add(new NamedAdditionalFile(AdditionalFileType.SOURCES_JAR, Util.resolveFileProperty(project, file).get()));
	}

	/**
	 * Creates a dev JAR AdditionalFile Container
	 *
	 * @param file the file
	 */
	public void devJar(final Object file) {
		this.additionalFiles.add(new NamedAdditionalFile(AdditionalFileType.DEV_JAR, Util.resolveFileProperty(project, file).get()));
	}

	/**
	 * Creates a Javadoc JAR AdditionalFile Container
	 *
	 * @param file the file
	 */
	public void javadocJar(final Object file) {
		this.additionalFiles.add(new NamedAdditionalFile(AdditionalFileType.JAVADOC_JAR, Util.resolveFileProperty(project, file).get()));
	}

	/**
	 * Creates a signature AdditionalFile Container
	 *
	 * @param file the file
	 */
	public void signature(final Object file) {
		this.additionalFiles.add(new NamedAdditionalFile(AdditionalFileType.SIGNATURE, Util.resolveFileProperty(project, file).get()));
	}

	/**
	 * Creates another AdditionalFile Container
	 *
	 * @param file the file
	 */
	public void other(final Object file) {
		this.additionalFiles.add(new NamedAdditionalFile(AdditionalFileType.OTHER, Util.resolveFileProperty(project, file).get()));
	}
}
