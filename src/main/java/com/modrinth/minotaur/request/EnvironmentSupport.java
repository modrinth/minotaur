package com.modrinth.minotaur.request;

import com.google.gson.annotations.SerializedName;
import java.util.Locale;

/**
 * The enum representing the environment support state for client or server side.
 */
public enum EnvironmentSupport {
	/**
	 * The environment is required for this version.
	 */
	@SerializedName("required")
	REQUIRED,

	/**
	 * The environment is optional for this version.
	 */
	@SerializedName("optional")
	OPTIONAL,

	/**
	 * The environment is unsupported for this version.
	 */
	@SerializedName("unsupported")
	UNSUPPORTED,

	/**
	 * The environment compatibility state is unknown.
	 */
	@SerializedName("unknown")
	UNKNOWN;

	/**
	 * Parses a string into an {@link EnvironmentSupport} enum instance ignoring case.
	 *
	 * @param value The string value to parse
	 * @return The matching {@link EnvironmentSupport} constant
	 * @throws IllegalArgumentException if no matching constant is found
	 */
	public static EnvironmentSupport from(String value) {
		if (value == null) {
			return null;
		}
		return valueOf(value.trim().toUpperCase(Locale.ROOT));
	}
}
