package com.modrinth.minotaur;

import masecla.modrinth4j.client.agent.UserAgent;
import masecla.modrinth4j.main.ModrinthAPI;
import org.gradle.api.Project;
import org.gradle.api.file.RegularFile;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;

/**
 * Internal utility methods to make things easier and deduplicated
 */
@ApiStatus.Internal
class Util {
	/**
	 * @param ext {@link ModrinthExtension} instance for obtaining info from
	 * @param log Gradle logger used when non-PATs are in use
	 * @return A valid {@link ModrinthAPI} instance
	 */
	static ModrinthAPI api(ModrinthExtension ext, Logger log) {
		String url = ext.getApiUrl().get();
		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}

		UserAgent agent = UserAgent.builder()
			.authorUsername("modrinth")
			.projectName("minotaur")
			.projectVersion(Util.class.getPackage().getImplementationVersion())
			.contact(ext.getProjectId().get() + "/" + resolveVersionNumber(ext, "unknown"))
			.build();

		String token = ext.getToken().get();
		if (token.startsWith("mra")) {
			throw new RuntimeException("Token must be a personal-access token, not a session token!");
		} else if (!token.startsWith("mrp")) {
			log.log(LogLevel.WARN, "Using GitHub tokens for authentication is deprecated. Please begin to use personal-access tokens.");
		}

		return ModrinthAPI.rateLimited(agent, url, token);
	}

	/**
	 * Safely resolves the version number.
	 *
	 * @param ext The {@link ModrinthExtension} to resolve the version from
	 * @param fallback Value if no value is provided in the extension
	 * @return The extension version number if set; otherwise, the fallback param.
	 */
	static String resolveVersionNumber(ModrinthExtension ext, String fallback) {
		if (ext.getVersionNumber().getOrNull() == null) {
			ext.getVersionNumber().set(fallback);
		}
		return ext.getVersionNumber().get();
	}

	/**
	 * Attempts to resolve a file using an arbitrary object provided by a user defined gradle
	 * task.
	 *
	 * @param in The arbitrary input object from the user.
	 * @return A file handle for the resolved input. If the input can not be resolved this will be null or the fallback.
	 */
	@Nullable
	static File resolveFile(Object in) {
		if (in == null) {
			// If input is null we can't really do anything...
			return null;
		} else if (in instanceof File) {
			// If the file is a Java file handle no additional handling is needed.
			return (File) in;
		} else if (in instanceof AbstractArchiveTask) {
			// Grabs the file from an archive task. Allows build scripts to do things like the jar task directly.
			return ((AbstractArchiveTask) in).getArchiveFile().get().getAsFile();
		} else if (in instanceof TaskProvider<?>) {
			// Grabs the file from an archive task wrapped in a provider. Allows Kotlin DSL buildscripts to also specify
			// the jar task directly, rather than having to call #get() before running.
			Object provided = ((TaskProvider<?>) in).get();

			// Check to see if the task provided is actually an AbstractArchiveTask.
			if (provided instanceof AbstractArchiveTask) {
				return ((AbstractArchiveTask) provided).getArchiveFile().get().getAsFile();
			}
		} else if (in instanceof Path) {
			return ((Path) in).toFile();
		} else if (in instanceof String) {
			return new File((String) in);
		}

		throw new RuntimeException("Could not resolve file " + in);
	}

	static Provider<RegularFile> resolveFileProperty(Project project, Object in) {
		if (in == null) {
			// If input is null we can't really do anything...
			return project.getObjects().fileProperty();
		} else if (in instanceof File) {
			// If the file is a Java file handle no additional handling is needed.
			return project.getLayout().file(project.provider(() -> (File) in));
		} else if (in instanceof AbstractArchiveTask) {
			// Grabs the file from an archive task. Allows build scripts to do things like the jar task directly.
			return ((AbstractArchiveTask) in).getArchiveFile();
		} else if (in instanceof TaskProvider<?>) {
			// Grabs the file from an archive task wrapped in a provider. Allows Kotlin DSL buildscripts to also specify
			// the jar task directly, rather than having to call #get() before running.
			Object provided = ((TaskProvider<?>) in).get();

			return ((TaskProvider<?>) in).flatMap(task -> {
				// Check to see if the task provided is actually an AbstractArchiveTask.
				if (provided instanceof AbstractArchiveTask) {
					return ((AbstractArchiveTask) provided).getArchiveFile();
				}
				return project.getLayout().file(project.provider(() -> project.file(in)));
			});
		}

		// None of the previous checks worked. Fall back to Gradle's built-in file resolution mechanics.
		return project.getLayout().file(project.provider(() -> project.file(in)));
	}
}
