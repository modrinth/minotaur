package com.modrinth.minotaur.request;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;

public interface ModrinthApiSettings {
	/**
	 * This should not be changed unless you know what you're doing. Its main use case is for debug, development, or
	 * advanced user configurations.
	 *
	 * @return The URL used for communicating with Modrinth.
	 */
	@Input
	Property<String> getApiUrl();

	/**
	 * Make sure you keep this private!
	 *
	 * @return The API token used to communicate with Modrinth.
	 */
	@Internal
	Property<String> getToken();

	/**
	 * @return The ID of the project to upload the file to.
	 */
	@Input
	Property<String> getProjectId();

	/**
	 * @return The version number of the project being uploaded.
	 */
	@Input
	Property<String> getVersionNumber();
}
