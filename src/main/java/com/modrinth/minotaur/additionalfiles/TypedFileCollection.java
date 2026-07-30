package com.modrinth.minotaur.additionalfiles;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;

/**
 * Defines a set of additional files to be uploaded to a Modrinth project, along with their type.
 */
public interface TypedFileCollection {
	/**
	 * @return files to be uploaded
	 */
	@InputFiles
	ConfigurableFileCollection getFiles();

	/**
	 * @return the type of these files
	 */
	@Input
	Property<AdditionalFileType> getType();
}
