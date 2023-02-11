package com.modrinth.minotaur;

import com.google.gson.JsonObject;
import masecla.modrinth4j.endpoints.project.ModifyProject.ProjectModifications;
import masecla.modrinth4j.main.ModrinthAPI;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import java.util.Objects;
import java.util.regex.Pattern;

import static com.modrinth.minotaur.Util.api;
import static com.modrinth.minotaur.Util.ext;

/**
 * A task used to communicate with Modrinth for the purpose of syncing project body with, for example, a README.
 */
public class TaskModrinthSyncBody extends DefaultTask {
	/**
	 * Uploads a body to a project, both of which are specified in {@link ModrinthExtension}.
	 */
	@TaskAction
	public void apply() {
		getLogger().lifecycle("Minotaur: {}", getClass().getPackage().getImplementationVersion());
		ModrinthExtension ext = ext(getProject());
		try {
			if (ext.getSyncBodyFrom() == null) {
				throw new GradleException("Sync project body task was called, but `syncBodyFrom` was null!");
			}

			ModrinthAPI api = api(getProject());

			// This isn't used until later, but resolve it early anyway to throw invalid IDs early
			String id = Objects.requireNonNull(
				api.projects().getProjectIdBySlug(ext.getProjectId().get()).join(),
				"Failed to resolve project ID: " + ext.getProjectId().get()
			);
			getLogger().debug("Syncing body to project {}", id);

			Pattern excludeRegex = Pattern.compile("<!-- modrinth_exclude\\.start -->.*?<!-- modrinth_exclude\\.end -->", Pattern.DOTALL);
			String body = ext.getSyncBodyFrom().get().replaceAll("\r\n", "\n");
			body = excludeRegex.matcher(body).replaceAll("");

			if (ext.getDebugMode().get()) {
				JsonObject data = new JsonObject();
				data.addProperty("body", body);
				getLogger().lifecycle("Full data to be sent for upload: {}", data);
				getLogger().lifecycle("Minotaur debug mode is enabled. Not going to upload the body.");
				return;
			}

			api.projects().modify(id, ProjectModifications.builder().body(body).build()).join();
			getLogger().lifecycle("Successfully synced body to project {}.", ext.getProjectId().get());
		} catch (Exception e) {
			if (ext.getFailSilently().get()) {
				getLogger().info("Failed to sync body to Modrinth. Check logs for more info.");
				getLogger().error("Modrinth body sync failed silently.", e);
			} else {
				throw new GradleException("Failed to sync project body! " + e.getMessage(), e);
			}
		}
	}
}
