package com.modrinth.minotaur;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.modrinth.minotaur.responses.ResponseError;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Internal utility methods to make things easier and deduplicated
 */
class Util {
    static HttpClient createHttpClient() {
        return HttpClientBuilder.create()
            .setDefaultRequestConfig(
                RequestConfig.custom()
                    .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                    .build()
            )
            .build();
    }

    static Gson createGsonInstance() {
        return new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Provides the upload API endpoint to use.
     *
     * @return The upload API endpoint.
     */
    static String getUploadEndpoint(Project project) {
        final ModrinthExtension extension = project.getExtensions().getByType(ModrinthExtension.class);
        String apiUrl = extension.getApiUrl().get();
        return apiUrl + (apiUrl.endsWith("/") ? "" : "/");
    }

    /**
     * Returns a project ID from a project ID or slug
     *
     * @param projectId ID or slug of the project to resolve
     * @param project   Gradle project to resolve upload endpoint
     * @return ID of the resolved project
     */
    static String resolveId(String projectId, Project project) throws IOException {
        HttpClient client = createHttpClient();
        HttpGet get = new HttpGet(getUploadEndpoint(project) + "project/" + projectId);
        get.addHeader("Authorization", project.getExtensions().getByType(ModrinthExtension.class).getToken().get());
        HttpResponse response = client.execute(get);

        int code = response.getStatusLine().getStatusCode();
        if (code != 200) {
            ResponseError errorInfo = createGsonInstance().fromJson(EntityUtils.toString(response.getEntity()), ResponseError.class);
            if (errorInfo == null) {
                errorInfo = new ResponseError();
            }
            String error = String.format("Project ID resolution for %s failed! Status: %s Error: %s Reason: %s", projectId, code, errorInfo.getError(), errorInfo.getDescription());
            project.getLogger().error(error);
            throw new GradleException(error);
        }

        String returned = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        JsonElement element = JsonParser.parseString(returned);
        if (!element.isJsonObject() || !element.getAsJsonObject().has("id")) {
            String error = "Invalid API response during project ID resolution! Expected JSON with an ID field but got: " + returned;
            project.getLogger().error(error);
            throw new GradleException(error, new IllegalStateException(error));
        }
        return element.getAsJsonObject().get("id").getAsString();
    }
}
