package com.modrinth.minotaur;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.modrinth.minotaur.responses.ResponseError;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Internal utility methods to make things easier and deduplicated
 */
class Util {
    /**
     * @return The {@link ModrinthExtension} for the project
     */
    static ModrinthExtension getExtension() {
        return Minotaur.project.getExtensions().getByType(ModrinthExtension.class);
    }

    /**
     * @return A new {@link HttpClient} with our desired settings
     */
    static HttpClient createHttpClient() {
        RequestConfig rc = RequestConfig.custom().setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();
        String ua = "modrinth/minotaur " + Util.class.getPackage().getImplementationVersion();
        return HttpClientBuilder.create().setDefaultRequestConfig(rc).setUserAgent(ua).build();
    }

    /**
     * @return A new {@link Gson} instance with our desired settings
     */
    static Gson createGsonInstance() {
        return new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Provides the upload API endpoint to use.
     *
     * @return The upload API endpoint.
     */
    static String getUploadEndpoint() {
        String apiUrl = getExtension().getApiUrl().get();
        return apiUrl + (apiUrl.endsWith("/") ? "" : "/");
    }

    /**
     * Returns a project ID from a project ID or slug
     *
     * @param projectId ID or slug of the project to resolve
     * @return ID of the resolved project
     */
    static String resolveId(String projectId) throws IOException {
        return resolveId(projectId, Minotaur.project.getLogger());
    }

    /**
     * Returns a project ID from a project ID or slug
     *
     * @param projectId ID or slug of the project to resolve
     * @param log Logger to use
     * @return ID of the resolved project
     */
    static String resolveId(String projectId, Logger log) throws IOException {
        HttpClient client = createHttpClient();
        HttpGet get = new HttpGet(String.format("%sproject/%s/check", getUploadEndpoint(), projectId));
        get.addHeader("Authorization", getExtension().getToken().get());
        HttpResponse response = client.execute(get);

        int code = response.getStatusLine().getStatusCode();
        if (code != 200) {
            ResponseError errorInfo = createGsonInstance().fromJson(EntityUtils.toString(response.getEntity()), ResponseError.class);
            if (errorInfo == null) {
                errorInfo = new ResponseError();
            }
            String error = String.format("Project ID resolution for %s failed! Status: %s Error: %s Reason: %s", projectId, code, errorInfo.getError(), errorInfo.getDescription());
            log.error(error);
            throw new GradleException(error);
        }

        String returned = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        JsonElement element = JsonParser.parseString(returned);
        if (!element.isJsonObject() || !element.getAsJsonObject().has("id")) {
            String error = "Invalid API response during project ID resolution! Expected JSON with an ID field but got: " + returned;
            log.error(error);
            throw new GradleException(error, new IllegalStateException(error));
        }
        return element.getAsJsonObject().get("id").getAsString();
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
        }

        // None of the previous checks worked. Fall back to Gradle's built-in file resolution mechanics.
        return Minotaur.project.file(in);
    }
}
