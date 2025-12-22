package com.modrinth.minotaur.additionalfiles;

import com.google.gson.annotations.SerializedName;
import masecla.modrinth4j.model.version.ProjectVersion.ProjectDependencyType;

import java.util.Locale;

/**
 * The enum representing the additional file types supported by Modrinth.
 */
public enum AdditionalFileType {
	/**
	 * The file is a resource pack file that must be used alongside the file. Primarily meant for data
	 * pack projects.
	 */
	@SerializedName("required-resource-pack")
	REQUIRED_RESOURCE_PACK,

	/**
	 * The file is a resource pack file that can be used alongside the file. Primarily meant for data
	 * pack projects.
	 */
	@SerializedName("optional-resource-pack")
	OPTIONAL_RESOURCE_PACK,

	/**
	 * The file is a JAR containing source code.
	 */
	@SerializedName("sources-jar")
	SOURCES_JAR,

	/**
	 * The file is a JAR containing unmapped or development code.
	 */
	@SerializedName("dev-jar")
	DEV_JAR,

	/**
	 * The file is a JAR containing Javadoc documentation.
	 */
	@SerializedName("javadoc-jar")
	JAVADOC_JAR,

	/**
	 * The file is a signature file (asc, gpg, or sig).
	 */
	@SerializedName("signature")
	SIGNATURE,

	/**
	 * The file is a different type of additional file.
	 */
	@SerializedName("")
	OTHER;

	public String toString() {
		return this.name().toLowerCase(Locale.ROOT);
	}

}
