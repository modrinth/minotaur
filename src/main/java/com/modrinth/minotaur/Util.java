package com.modrinth.minotaur;

import masecla.modrinth4j.client.agent.UserAgent;
import masecla.modrinth4j.main.ModrinthAPI;
import org.gradle.api.Project;
import org.jetbrains.annotations.ApiStatus;

/**
 * Internal utility methods to make things easier and deduplicated
 */
@ApiStatus.Internal
public class Util {
	/**
	 * @param project Gradle project for getting various info from
	 * @return A valid {@link ModrinthAPI} instance
	 */
	static ModrinthAPI api(Project project) {
		ModrinthExtension ext = ext(project);
		String url = ext.getApiUrl().get();
		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}

		UserAgent agent = UserAgent.builder()
			.authorUsername("modrinth")
			.projectName("minotaur")
			.projectVersion(Util.class.getPackage().getImplementationVersion())
			.contact(ext.getProjectId().get() + "/" + resolveVersionNumber(project))
			.build();

		String token = ext.getToken().get();
		if (token.startsWith("mra")) {
			throw new RuntimeException("Token must be a personal-access token, not a session token!");
		} else if (!token.startsWith("mrp")) {
			project.getLogger().warn("Using GitHub tokens for authentication is deprecated. Please begin to use personal-access tokens.");
		}

		return ModrinthAPI.rateLimited(agent, url, token);
	}

	/**
	 * @param project Gradle project for getting various info from
	 * @return The {@link ModrinthExtension} for the project
	 */
	public static ModrinthExtension ext(Project project) {
		return project.getExtensions().getByType(ModrinthExtension.class);
	}

	/**
	 * Safely resolves the version number.
	 *
	 * @param project The Gradle project to resolve the extension and version from
	 * @return The extension version number if set; otherwise, the Gradle project version.
	 */
	public static String resolveVersionNumber(Project project) {
		ModrinthExtension ext = ext(project);
		if (ext.getVersionNumber().getOrNull() == null) {
			ext.getVersionNumber().set(project.getVersion().toString());
		}
		return ext.getVersionNumber().get();
	}
}
