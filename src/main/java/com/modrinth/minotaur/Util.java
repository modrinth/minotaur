package com.modrinth.minotaur;

import masecla.modrinth4j.client.agent.UserAgent;
import masecla.modrinth4j.exception.EndpointException;
import masecla.modrinth4j.main.ModrinthAPI;
import masecla.modrinth4j.model.user.ModrinthUser;
import org.gradle.api.Project;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Objects;

/**
 * Internal utility methods to make things easier and deduplicated
 */
@ApiStatus.Internal
class Util {
	/**
	 * @param project Gradle project for getting various info from
	 * @return A valid {@link ModrinthAPI} instance
	 * @throws EndpointException when the request to validate the token fails
	 */
	static ModrinthAPI api(Project project) throws EndpointException, NullPointerException {
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

		String token = ext(project).getToken().get();
		ModrinthAPI api = ModrinthAPI.rateLimited(agent, url, token);

		// Ensure validity of token unless in Minotaur CI
		if (token.equals("dummy_token_for_CI")) {
			project.getLogger().info("Skipping token validation (GitHub repo {})", System.getenv("GITHUB_REPOSITORY"));
		} else {
			ModrinthUser user = api.users().getSelf().join();
			String username = Objects.requireNonNull(user.getUsername(), "Failed to resolve username from token");
			project.getLogger().debug("Signed in as user {}", username);
		}

		return api;
	}

	/**
	 * @param project Gradle project for getting various info from
	 * @return The {@link ModrinthExtension} for the project
	 */
	static ModrinthExtension ext(Project project) {
		return project.getExtensions().getByType(ModrinthExtension.class);
	}

	/**
	 * Safely resolves the version number.
	 *
	 * @param project The Gradle project to resolve the extension and version from
	 * @return The extension version number if set; otherwise, the Gradle project version.
	 */
	static String resolveVersionNumber(Project project) {
		ModrinthExtension ext = ext(project);
		if (ext.getVersionNumber().getOrNull() == null) {
			ext.getVersionNumber().set(project.getVersion().toString());
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
	static File resolveFile(Project project, Object in) {
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
		}

		// None of the previous checks worked. Fall back to Gradle's built-in file resolution mechanics.
		return project.file(in);
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
