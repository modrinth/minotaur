package com.modrinth.minotaur.additionalfiles.container;

import com.modrinth.minotaur.additionalfiles.AdditionalFile;
import com.modrinth.minotaur.additionalfiles.AdditionalFileType;
import org.gradle.api.Named;
import org.gradle.api.file.RegularFile;
import org.jetbrains.annotations.NotNull;

/**
 * Defines a Named AdditionalFile for our NamedAdditionalFileContainer.
 */
public class NamedAdditionalFile implements Named {
	private final AdditionalFileType additionalFileType;
	private final RegularFile file;

	/**
	 * Instantiates a new NamedAdditionalFile.
	 *
	 * @param additionalFileType the AdditionalFileType
	 * @param file               the file to upload
	 */
	protected NamedAdditionalFile(AdditionalFileType additionalFileType, RegularFile file) {
		this.additionalFileType = additionalFileType;
		this.file = file;
	}

	/**
	 * @return the file name
	 */
	@NotNull
	@Override
	public String getName() {
		return this.file.getAsFile().getName();
	}

	/**
	 * Gets the AdditionalFileType as String.
	 *
	 * @return the type
	 */
	public AdditionalFileType getAdditionalFileType() {
		return this.additionalFileType;
	}

	/**
	 * Return this as a never null {@link AdditionalFile}.
	 *
	 * @return {@link AdditionalFile} dynamically created AdditionalFile
	 */
	@NotNull
	public AdditionalFile getAdditionalFile() {
		return new AdditionalFile(this.additionalFileType, this.file);
	}
}
