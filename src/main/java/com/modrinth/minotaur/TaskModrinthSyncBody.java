package com.modrinth.minotaur;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.modrinth.minotaur.responses.ResponseError;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * A task used to communicate with Modrinth for the purpose of syncing project body with, for example, a README.
 */
public class TaskModrinthSyncBody extends DefaultTask {
    /**
     * Constant gson instance used for deserializing the API responses.
     */
    private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * The extension used for getting the data supplied in the buildscript.
     */
    private final ModrinthExtension extension = getProject().getExtensions().getByType(ModrinthExtension.class);
    
    /**
     * The response from the API when the body failed to upload.
     */
    @Nullable
    public ResponseError errorInfo = null;

    /**
     * Uploads a body to a project, both of which are specified in {@link ModrinthExtension}.
     */
    @TaskAction
    public void apply() {
        this.getLogger().lifecycle("Minotaur: {}", this.getClass().getPackage().getImplementationVersion());

        if (extension.getSyncBodyFrom().getOrNull() == null) {
            this.getProject().getLogger().error("Sync project body task was called, but `syncBodyFrom` was null!");
            throw new GradleException("Sync project body task was called, but `syncBodyFrom` was null!");
        }

        String excludeRegex = "(?m)<!-- modrinth_exclude\\.start -->(.|\n)*?<!-- modrinth_exclude\\.end -->";

        final HttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES).build()).build();
        final HttpPatch patch = new HttpPatch(this.getUploadEndpoint());

        patch.addHeader("Authorization", extension.getToken().get());

        JsonObject data = new JsonObject();
        data.addProperty("body", extension.getSyncBodyFrom().get().replaceAll(excludeRegex, "").replaceAll("\\r\\n", "\n"));

        if (extension.getDebugMode().get()) {
            this.getProject().getLogger().lifecycle("Full data to be sent for upload: {}", data);
            this.getProject().getLogger().lifecycle("Minotaur debug mode is enabled. Not going to upload the body.");
            return;
        }

        try {
            patch.setEntity(new StringEntity(GSON.toJson(data), ContentType.APPLICATION_JSON));
        } catch (StackOverflowError e) {
            String error = "StackOverflowError whilst trying to parse modrinth_exclude tags; please make the amount of text within each of these tags smaller";
            this.getProject().getLogger().error(error);
            throw new GradleException(error, e);
        }

        try {
            final HttpResponse response = client.execute(patch);
            final int status = response.getStatusLine().getStatusCode();

            if (status == 204) {
                this.getProject().getLogger().lifecycle("Successfully synced body to project {}.", extension.getProjectId().get());
            } else {
                this.errorInfo = GSON.fromJson(EntityUtils.toString(response.getEntity()), ResponseError.class);
                this.getProject().getLogger().error("Syncing failed! Status: {} Error: {} Reason: {}", status, this.errorInfo.getError(), this.errorInfo.getDescription());
                throw new GradleException("Syncing failed! Status: " + status + " Reason: " + this.errorInfo.getDescription());
            }
        } catch (final IOException e) {
            this.getProject().getLogger().error("Failed to sync project body!", e);
            throw new GradleException("Failed to sync project body!", e);
        }
    }

    /**
     * Provides the upload API endpoint to use.
     *
     * @return The upload API endpoint.
     */
    private String getUploadEndpoint() {
        String apiUrl = extension.getApiUrl().get();
        return apiUrl + (apiUrl.endsWith("/") ? "" : "/") + "project/" + extension.getProjectId().get();
    }
}
