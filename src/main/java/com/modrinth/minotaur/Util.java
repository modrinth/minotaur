package com.modrinth.minotaur;

import masecla.modrinth4j.client.agent.UserAgent;
import masecla.modrinth4j.main.ModrinthAPI;
import org.gradle.api.Project;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

/**
 * Internal utility methods to make things easier and deduplicated
 */
@ApiStatus.Internal
public class Util {
	/**
	 * @return A valid {@link ModrinthAPI} instance
	 */
	static ModrinthAPI api(Logger logger, String apiUrl, String token, String projectId, String versionNumber) {
		UserAgent agent = buildUserAgent(logger, token, projectId, versionNumber);
		return ModrinthAPI.rateLimited(agent, stripTrailingSlash(apiUrl), token);
	}

	public static String stripTrailingSlash(String url) {
		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}
		return url;
	}

	public static UserAgent buildUserAgent(Logger logger, String token, String projectId, String versionNumber) {
		UserAgent agent = UserAgent.builder()
			.authorUsername("modrinth")
			.projectName("minotaur")
			.projectVersion(Util.class.getPackage().getImplementationVersion())
			.contact(projectId + "/" + versionNumber)
			.build();

		if (token.startsWith("mra")) {
			throw new RuntimeException("Token must be a personal-access token, not a session token!");
		} else if (!token.startsWith("mrp")) {
			logger.warn("Using GitHub tokens for authentication is deprecated. Please begin to use personal-access tokens.");
		}
		return agent;
	}

	/**
	 * @param project Gradle project for getting various info from
	 * @return The {@link ModrinthExtension} for the project
	 */
	@Deprecated
	public static ModrinthExtension ext(Project project) {
		return project.getExtensions().getByType(ModrinthExtension.class);
	}

	/**
	 * Safely resolves the version number.
	 *
	 * @param project The Gradle project to resolve the extension and version from
	 * @return The extension version number if set; otherwise, the Gradle project version.
	 */
	@Deprecated
	public static String resolveVersionNumber(Project project) {
		ModrinthExtension ext = ext(project);
		if (ext.getVersionNumber().getOrNull() == null) {
			ext.getVersionNumber().set(project.getVersion().toString());
		}
		return ext.getVersionNumber().get();
	}
}
