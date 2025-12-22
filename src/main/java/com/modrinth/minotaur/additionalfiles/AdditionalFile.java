package com.modrinth.minotaur.additionalfiles;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import masecla.modrinth4j.model.version.ProjectVersion.ProjectAdditionalFile;
import masecla.modrinth4j.model.version.ProjectVersion.ProjectAdditionalFileType;
import org.jetbrains.annotations.ApiStatus;

import java.util.Locale;

/**
 * Represents an additional file.
 */
public class AdditionalFile {

	/**
	 * The {@link ProjectAdditionalFileType} of the additional file.
	 */
	@Expose
	@SerializedName("additional_file_type")
	private final ProjectAdditionalFileType additionalFileType;

	/**
	 * Creates a new additional file relationship.
	 *
	 * @param type The type of additional file being created.
	 */
	@ApiStatus.Internal
	AdditionalFile(AdditionalFileType type) {
		this.additionalFileType = type.toNew();
	}

	/**
	 * Creates a new additional file relationship.
	 *
	 * @param type The type of additional file being created.
	 */
	@ApiStatus.Internal
	AdditionalFile(String type) {
		this.additionalFileType = ProjectAdditionalFileType.valueOf(type.toUpperCase(Locale.ROOT));
	}

	/**
	 * @return {@link #additional fileType}
	 */
	ProjectAdditionalFileType getAdditionalFileType() {
		return this.additionalFileType;
	}

	/**
	 * @return a {@link ProjectAdditionalFile} instance from a {@link AdditionalFile}
	 */
	public ProjectAdditionalFile toNew() {
		return new ProjectAdditionalFile();
	}

	/**
	 * @param newDep the {@link ProjectAdditionalFile} to convert to a {@link AdditionalFile}
	 * @return a converted {@link AdditionalFile}
	 */
	public static AdditionalFile fromNew(ProjectAdditionalFile newDep) {
		return new AdditionalFile(newDep.getProjectId(), newDep.getAdditionalFileType().name());
	}
}
