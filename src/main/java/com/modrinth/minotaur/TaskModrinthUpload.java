package com.modrinth.minotaur;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.modrinth.minotaur.additionalfiles.TypedFileCollection;
import com.modrinth.minotaur.dependencies.Dependency;
import com.modrinth.minotaur.masecla.modrinth4j.endpoints.version.TemporaryCreateVersion;
import com.modrinth.minotaur.masecla.modrinth4j.endpoints.version.TemporaryCreateVersion.TemporaryCreateVersionRequest;
import com.modrinth.minotaur.request.ModrinthApiSettings;
import com.modrinth.minotaur.responses.ResponseUpload;
import masecla.modrinth4j.main.ModrinthAPI;
import masecla.modrinth4j.model.version.ProjectVersion;
import masecla.modrinth4j.model.version.ProjectVersion.ProjectDependency;
import masecla.modrinth4j.model.version.ProjectVersion.VersionType;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static com.modrinth.minotaur.Util.api;

/**
 * A task used to communicate with Modrinth for the purpose of uploading build artifacts.
 */
@UntrackedTask(because = "uploads to Modrinth")
public abstract class TaskModrinthUpload extends DefaultTask {
	/**
	 * The response from the API when the file was uploaded successfully.
	 */
	@Nullable
	public ProjectVersion newVersion = null;

	/**
	 * The response from the API when the file was uploaded successfully.
	 *
	 * @deprecated Please use {@link #newVersion} instead
	 */
	@SuppressWarnings("DeprecatedIsStillUsed")
	@Nullable
	@Deprecated
	@ApiStatus.ScheduledForRemoval(inVersion = "3.0.0")
	public ResponseUpload uploadInfo = null;

	/**
	 * Checks if the upload was successful or not.
	 *
	 * @return Whether the file was successfully uploaded.
	 * @deprecated This check should be done manually
	 */
	@SuppressWarnings("unused")
	@Deprecated
	@ApiStatus.ScheduledForRemoval(inVersion = "3.0.0")
	public boolean wasUploadSuccessful() {
		return newVersion != null;
	}

	/**
	 * @return the main file to upload
	 */
	@InputFile
	public abstract RegularFileProperty getFile();

	/**
	 * @return additional files to upload alongside the main file
	 * @see #getUntypedAdditionalFiles()
	 */
	@Nested
	public abstract ListProperty<TypedFileCollection> getAdditionalFiles();

	/**
	 * Gets a collection of additional files to upload alongside the main file.
	 * Similar to {@link #getAdditionalFiles()}, but does not specify the type of the files, and instead relies on
	 * the file name to determine the type.
	 *
	 * @return additional files to upload alongside the main file
	 */
	@InputFiles
	public abstract ConfigurableFileCollection getUntypedAdditionalFiles();

	/**
	 * @return the changelog text
	 */
	@Input
	public abstract Property<String> getChangelog();

	/**
	 * @return whether the build should continue even if the upload failed
	 */
	@Input
	public abstract Property<Boolean> getFailSilently();

	/**
	 * @return The ID of the project to upload the file to.
	 */
	@Input
	public abstract Property<String> getProjectId();

	/**
	 * @return the version number of the build
	 */
	@Input
	public abstract Property<String> getVersionNumber();

	/**
	 * @return the version name of the build
	 */
	@Input
	public abstract Property<String> getVersionName();

	/**
	 * @return the Modrinth API settings
	 */
	@Nested
	public abstract ModrinthApiSettings getApiSettings();

	/**
	 * @return whether the task should only simulate the upload without actually performing it
	 */
	@Input
	public abstract Property<Boolean> getIsDryRun();

	/**
	 * @return the mod loaders which this build supports
	 */
	@Input
	public abstract ListProperty<String> getLoaders();

	/**
	 * @return the game versions which this build supports
	 */
	@Input
	public abstract ListProperty<String> getGameVersions();

	/**
	 * @return the Modrinth project dependencies of this build
	 */
	@Input
	public abstract ListProperty<Dependency> getDependencies();

	/**
	 * @return the release type for the project
	 * @see VersionType
	 */
	@Input
	public abstract Property<String> getVersionType();

	/**
	 * Defines what to do when the Modrinth upload task is invoked.
	 * <ol>
	 *   <li>Attempts to automatically resolve various metadata items if not specified, throwing an exception if some
	 *   things still don't have anything set</li>
	 *   <li>Resolves each file or task to be uploaded, ensuring they're all valid</li>
	 *   <li>Uploads these files to the Modrinth API under a new version</li>
	 * </ol>
	 * This is all in a try/catch block so that, if {@link #getFailSilently()} is enabled, it won't
	 * fail the build if it fails to upload the version to Modrinth.
	 */
	@TaskAction
	public void apply() {
		try {
			if (getLoaders().get().isEmpty()) {
				throw new InvalidUserDataException("Cannot upload to Modrinth: no loaders specified!");
			}

			if (getGameVersions().get().isEmpty()) {
				throw new InvalidUserDataException("Cannot upload to Modrinth: no game versions specified!");
			}

			VersionType versionType;
			try {
				versionType = VersionType.valueOf(getVersionType().get().toUpperCase(Locale.ROOT));
			} catch (IllegalArgumentException e) {
				throw new InvalidUserDataException("Cannot upload to Modrinth: invalid version type specified: " + getVersionType().get(), e);
			}

			getLogger().lifecycle("Minotaur: {}", getClass().getPackage().getImplementationVersion());
			ModrinthAPI api = api(getLogger(), getApiSettings());

			String slug = getProjectId().get();
			String id = api.projects().getProjectIdBySlug(slug).join();
			if (id == null) {
				if (getIsDryRun().get()) {
					getLogger().error("Cannot find project with id '{}'.", slug);
					id = "<unknown>";
				} else {
					throw new GradleException(String.format("Cannot find project with id '%s'", slug));
				}
			}
			getLogger().debug("Uploading version to project {}", id);

			// Convert each of our proto-dependencies to a proper Modrinth4J ProjectDependency
			List<ProjectDependency> dependencies = getDependencies().get().stream()
				.map(dependency -> dependency.toNew(api))
				.collect(Collectors.toList());

			// Get each of the files, starting with the primary file
			Map<File, String> files = new LinkedHashMap<>();
			files.put(getFile().get().getAsFile(), "primary");

			// Convert each of the Object files from the extension to a proper File
			getUntypedAdditionalFiles().forEach(resolvedFile -> {
				String fileType = guessUploadFileType(resolvedFile.getName());
				files.put(resolvedFile, fileType);
			});

			getAdditionalFiles().get().forEach(typedFiles -> {
				String type = typedFiles.getType().get().toString();
				typedFiles.getFiles().forEach(file -> files.put(file, type));
			});

			List<File> missingFiles = files.keySet().stream()
				.filter(file -> !file.isFile())
				.collect(Collectors.toList());
			if (!missingFiles.isEmpty()) {
				throw new GradleException("Missing some of the files we need to upload: " + missingFiles);
			}

			// Start construction of the actual request!
			TemporaryCreateVersionRequest data = TemporaryCreateVersionRequest.builder()
				.projectId(id)
				.versionNumber(getVersionNumber().get())
				.name(getVersionName().get())
				.changelog(getChangelog().get().replace("\r\n", "\n"))
				.versionType(versionType)
				.gameVersions(getGameVersions().get())
				.loaders(getLoaders().get())
				.dependencies(dependencies)
				.files(files)
				.build();

			// Return early in debug mode
			if (getIsDryRun().get()) {
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				getLogger().lifecycle("Full data to be sent for upload: {}", gson.toJson(data));
				getLogger().lifecycle("Minotaur debug mode is enabled. Not going to upload this version.");
				return;
			}

			// Execute the request
			ProjectVersion version = new TemporaryCreateVersion(getLogger(), getApiSettings())
				.sendRequest(data).join();
			//ProjectVersion version = api.versions().createProjectVersion(data).join();
			newVersion = version;
			//noinspection deprecation
			uploadInfo = new ResponseUpload(version);

			getLogger().lifecycle(
				"Successfully uploaded version {} to {} ({}) as version ID {}. {}",
				newVersion.getVersionNumber(),
				slug,
				id,
				newVersion.getId(),
				String.format(
					"%s/project/%s/version/%s",
					getApiSettings().getApiUrl().get().replaceFirst("-?api", "").replaceFirst("/?v2/?", "").replaceFirst("//\\.", "//"),
					id,
					newVersion.getId()
				)
			);
		} catch (Exception e) {
			if (getFailSilently().get()) {
				getLogger().info("Failed to upload to Modrinth. Check logs for more info.");
				getLogger().error("Modrinth upload failed silently.", e);
			} else if (e instanceof GradleException) {
				throw (GradleException) e;
			} else {
				throw new GradleException("Failed to upload file to Modrinth! " + e.getMessage(), e);
			}
		}
	}

	private static @Nullable String guessUploadFileType(String fileName) {
		String fileType = null;

		// No switches in Java 8 :(
		if (fileName.contains("-dev.jar")) {
			fileType = "dev-jar";
		} else if (fileName.contains("-sources.jar")) {
			fileType = "sources-jar";
		} else if (fileName.contains("-javadoc.jar")) {
			fileType = "javadoc-jar";
		} else if (fileName.contains("asc") || fileName.contains("gpg") || fileName.contains("sig")) {
			fileType = "signature";
		}
		return fileType;
	}
}
