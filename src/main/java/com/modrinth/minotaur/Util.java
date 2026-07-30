package com.modrinth.minotaur;

import com.modrinth.minotaur.request.ModrinthApiSettings;
import masecla.modrinth4j.client.agent.UserAgent;
import masecla.modrinth4j.main.ModrinthAPI;
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
	static ModrinthAPI api(Logger logger, ModrinthApiSettings settings) {
		validateToken(logger, settings.getToken().get());
		return ModrinthAPI.rateLimited(
			buildUserAgent(settings),
			stripTrailingSlash(settings.getApiUrl().get()),
			settings.getToken().get());
	}

	public static String stripTrailingSlash(String url) {
		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}
		return url;
	}

	public static UserAgent buildUserAgent(ModrinthApiSettings settings) {
		return UserAgent.builder()
			.authorUsername("modrinth")
			.projectName("minotaur")
			.projectVersion(Util.class.getPackage().getImplementationVersion())
			.contact(settings.getProjectId().get() + "/" + settings.getVersionNumber().get())
			.build();
	}

	public static void validateToken(Logger logger, String token) {
		if (token.startsWith("mra")) {
			throw new RuntimeException("Token must be a personal-access token, not a session token!");
		} else if (!token.startsWith("mrp")) {
			logger.warn("Using GitHub tokens for authentication is deprecated. Please begin to use personal-access tokens.");
		}
	}
}
