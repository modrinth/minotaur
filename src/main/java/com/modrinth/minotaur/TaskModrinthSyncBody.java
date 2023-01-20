package com.modrinth.minotaur;

import com.google.gson.JsonObject;
import masecla.modrinth4j.endpoints.project.ModifyProject.ModifyProjectRequest;
import masecla.modrinth4j.main.ModrinthAPI;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

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
        this.getLogger().lifecycle("Minotaur: {}", this.getClass().getPackage().getImplementationVersion());
        final ModrinthExtension ext = ext(this.getProject());
        try {
            if (ext.getSyncBodyFrom() == null) {
                this.getProject().getLogger().error("Sync project body task was called, but `syncBodyFrom` was null!");
                throw new GradleException("Sync project body task was called, but `syncBodyFrom` was null!");
            }

            final ModrinthAPI api = api(this.getProject());
            // This isn't used until later, but resolve it early anyway to throw invalid IDs early
            final String id = api.projects().getProjectIdBySlug(ext.getProjectId().get()).join();

            final Pattern excludeRegex = Pattern.compile("<!-- modrinth_exclude\\.start -->.*?<!-- modrinth_exclude\\.end -->", Pattern.DOTALL);
            String body = ext.getSyncBodyFrom().get().replaceAll("\r\n", "\n");
            body = excludeRegex.matcher(body).replaceAll("");

            if (ext.getDebugMode().get()) {
                JsonObject data = new JsonObject();
                data.addProperty("body", body);
                this.getProject().getLogger().lifecycle("Full data to be sent for upload: {}", data);
                this.getProject().getLogger().lifecycle("Minotaur debug mode is enabled. Not going to upload the body.");
                return;
            }

            api.projects().modify(id, ModifyProjectRequest.builder().body(body).build()).join();
            this.getProject().getLogger().lifecycle("Successfully synced body to project {}.", ext.getProjectId());
        } catch (final Exception e) {
            if (ext.getFailSilently().get()) {
                this.getLogger().info("Failed to sync body to Modrinth. Check logs for more info.");
                this.getLogger().error("Modrinth body sync failed silently.", e);
            } else {
                throw new GradleException("Failed to sync project body! " + e.getMessage(), e);
            }
        }
    }
}
