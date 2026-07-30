package com.modrinth.minotaur;

import com.google.gson.JsonObject;
import com.modrinth.minotaur.request.ModrinthApiSettings;
import masecla.modrinth4j.endpoints.project.ModifyProject.ProjectModifications;
import masecla.modrinth4j.main.ModrinthAPI;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.UntrackedTask;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A task used to communicate with Modrinth for the purpose of syncing project body with, for example, a README.
 */
@UntrackedTask(because = "edits data remotely on Modrinth")
public abstract class TaskModrinthSyncBody extends DefaultTask {
	/**
	 * @return the Modrinth API settings
	 */
	@Nested
	public abstract ModrinthApiSettings getApiSettings();

	/**
	 * @return The ID of the project to upload the file to.
	 */
	@Input
	public abstract Property<String> getProjectId();

	/**
	 * @return the file to sync the project's description from
	 */
	@Input
	public abstract Property<String> getSyncBodyFrom();

	/**
	 * @return whether the task should only simulate the changes without actually performing them
	 */
	@Input
	public abstract Property<Boolean> getIsDryRun();

	/**
	 * @return whether the build should continue even if the operation failed
	 */
	@Input
	public abstract Property<Boolean> getFailSilently();

	/**
	 * Uploads a body to a project, both of which are specified in {@link ModrinthExtension}.
	 */
	@TaskAction
	public void apply() {
		getLogger().lifecycle("Minotaur: {}", getClass().getPackage().getImplementationVersion());
		try {
			if (getSyncBodyFrom() == null) {
				throw new GradleException("Sync project body task was called, but `syncBodyFrom` was null!");
			}

			ModrinthAPI api = Util.api(getLogger(), getApiSettings());

			// This isn't used until later, but resolve it early anyway to throw invalid IDs early
			String id = Objects.requireNonNull(
				api.projects().getProjectIdBySlug(getProjectId().get()).join(),
				"Failed to resolve project ID: " + getProjectId().get()
			);
			getLogger().debug("Syncing body to project {}", id);

			Pattern excludeRegex = Pattern.compile("<!-- modrinth_exclude\\.start -->.*?<!-- modrinth_exclude\\.end -->", Pattern.DOTALL);
			String body = getSyncBodyFrom().get().replace("\r\n", "\n");
			body = excludeRegex.matcher(body).replaceAll("");

			if (getIsDryRun().get()) {
				JsonObject data = new JsonObject();
				data.addProperty("body", body);
				getLogger().lifecycle("Full data to be sent for upload: {}", data);
				getLogger().lifecycle("Minotaur debug mode is enabled. Not going to upload the body.");
				return;
			}

			api.projects().modify(id, ProjectModifications.builder().body(body).build()).join();
			getLogger().lifecycle("Successfully synced body to project {}.", getProjectId().get());
		} catch (Exception e) {
			if (getFailSilently().get()) {
				getLogger().info("Failed to sync body to Modrinth. Check logs for more info.");
				getLogger().error("Modrinth body sync failed silently.", e);
			} else if (e instanceof GradleException) {
				throw (GradleException) e;
			} else {
				throw new GradleException("Failed to sync project body! " + e.getMessage(), e);
			}
		}
	}
}
