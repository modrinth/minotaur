/*
 * Copyright 2023 TeamMT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.modrinth.minotaur.masecla.modrinth4j.endpoints.version;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.modrinth.minotaur.Util;
import com.modrinth.minotaur.request.ModrinthApiSettings;
import lombok.*;
import lombok.Builder.Default;
import masecla.modrinth4j.client.HttpClient;
import masecla.modrinth4j.client.instances.RatelimitedHttpClient;
import masecla.modrinth4j.endpoints.generic.Endpoint;
import masecla.modrinth4j.model.adapters.ISOTimeAdapter;
import masecla.modrinth4j.model.project.ProjectStatus;
import masecla.modrinth4j.model.search.FacetCollection;
import masecla.modrinth4j.model.team.ModrinthPermissionMask;
import masecla.modrinth4j.model.version.ProjectVersion;
import masecla.modrinth4j.model.version.ProjectVersion.ProjectDependency;
import masecla.modrinth4j.model.version.ProjectVersion.VersionType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * This endpoint is used to create a new version.
 */
public class TemporaryCreateVersion extends Endpoint<ProjectVersion, TemporaryCreateVersion.TemporaryCreateVersionRequest> {
	/**
	 * This class is used to represent the request.
	 */
	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class TemporaryCreateVersionRequest {
		/** The name of the version */
		@NonNull
		private String name;

		/** The version number of the version */
		@NonNull
		private String versionNumber;

		/** The changelog of the version */
		@NonNull
		private String changelog;

		/** The dependencies of the version */
		@Default
		private List<ProjectDependency> dependencies = new ArrayList<>();
		/** The game versions of the version */
		private List<String> gameVersions;
		/** The type of the version */
		private VersionType versionType;

		/** The loaders of the version */
		@NonNull
		private List<String> loaders;

		/** If the version is featured */
		private boolean featured;

		/** The project status */
		private ProjectStatus status;

		/** The requested status of the project */
		private ProjectStatus requestedStatus;

		/** The project ID of the version */
		@NonNull
		private String projectId;

		/** The primary file of the version */
		private String primaryFile;

		/** The file types of the version's additional files */
		private Map<String, String> fileTypes;

		/** The files of the version */
		@NonNull
		private transient List<String> fileNames;

		/** The file streams of the version */
		@NonNull
		private transient List<InputStream> fileStreams;

		/**
		 * This class is used to build the request.
		 */
		public static class TemporaryCreateVersionRequestBuilder {
			/**
			 * This method is used to add files to the request.
			 *
			 * @param files - The files to add.
			 * @return - The builder.
			 */
			@SneakyThrows
			public TemporaryCreateVersionRequestBuilder files(Map<File, String> files) {
				this.fileNames = new ArrayList<>();
				this.fileStreams = new ArrayList<>();
				this.fileTypes = new LinkedHashMap<>();

				for (Map.Entry<File, String> entry : files.entrySet()) {
					File file = entry.getKey();
					String fileType = entry.getValue();

					this.fileNames.add(file.getName());
					this.fileStreams.add(new FileInputStream(file));
					if (fileType != null && !fileType.equals("primary")) {
						this.fileTypes.put(file.getName(), fileType);
					}
				}

				return this;
			}

			/**
			 * This method is used to add files to the request.
			 *
			 * @param files - The files to add.
			 * @return - The builder.
			 */
			@SneakyThrows
			public TemporaryCreateVersionRequestBuilder files(File... files) {
				this.fileNames = new ArrayList<>();
				this.fileStreams = new ArrayList<>();

				for (int i = 0; i < files.length; i++) {
					this.fileNames.add(files[i].getName());
					this.fileStreams.add(new FileInputStream(files[i]));
				}
				return this;
			}

			/**
			 * This method is used to add files to the request.
			 *
			 * @param files - The files to add.
			 * @return - The builder.
			 */
			public TemporaryCreateVersionRequestBuilder files(List<File> files) {
				return this.files(files.toArray(new File[files.size()]));
			}
		}
	}

	/**
	 * This constructor is used to create a new instance of the endpoint.
	 */
	public TemporaryCreateVersion(Logger logger, ModrinthApiSettings settings) {
		super(httpClient(logger, settings), new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
			.registerTypeAdapter(FacetCollection.class, new FacetCollection.FacetAdapter())
			.registerTypeAdapter(ModrinthPermissionMask.class, new ModrinthPermissionMask.ModrinthPermissionMaskAdapter())
			.registerTypeAdapter(Instant.class, new ISOTimeAdapter())
			.create());
	}

	private static HttpClient httpClient(Logger logger, ModrinthApiSettings settings) {
		Util.validateToken(logger, settings.getToken().get());
		return new RatelimitedHttpClient(
			Util.buildUserAgent(settings),
			Util.stripTrailingSlash(settings.getApiUrl().get()),
			settings.getToken().get());
	}

	/**
	 * Returns the endpoint.
	 */
	@Override
	public String getEndpoint() {
		return "/version";
	}

	/**
	 * This method will send the request.
	 */
	@Override
	public CompletableFuture<ProjectVersion> sendRequest(TemporaryCreateVersionRequest request, Map<String, String> urlParams) {
		String url = getReplacedUrl(request, urlParams);
		return getClient().connect(url).thenApply(c -> {
			JsonObject jsonObject = getGson().toJsonTree(request).getAsJsonObject();

			if (request.getFileNames().size() > 1) {
				String primaryFile = request.getPrimaryFile();
				if (primaryFile == null || primaryFile.isEmpty()) {
					primaryFile = request.getFileNames().get(0);
				}
				jsonObject.addProperty("primary_file", primaryFile);
			}

			JsonArray array = new JsonArray();
			for (String filename : request.getFileNames()) {
				array.add(filename);
			}
			jsonObject.add("file_parts", array);

			MultipartBody.Builder body = new MultipartBody.Builder()
				.addFormDataPart("data", getGson().toJson(jsonObject));

			for (int i = 0; i < request.getFileNames().size(); i++) {
				body.addFormDataPart(request.getFileNames().get(i), request.getFileNames().get(i),
					RequestBody.create(this.readStream(request.getFileStreams().get(i))));
			}

			c.post(body.build());

			Response response = executeRequest(c);
			ProjectVersion version = this.checkBodyForErrors(response.body());
			return version;
		});
	}

	/**
	 * Returns the request class to use.
	 */
	@Override
	public TypeToken<TemporaryCreateVersionRequest> getRequestClass() {
		return TypeToken.get(TemporaryCreateVersionRequest.class);
	}

	/**
	 * Returns the response class to use.
	 */
	@Override
	public TypeToken<ProjectVersion> getResponseClass() {
		return TypeToken.get(ProjectVersion.class);
	}

	/**
	 * Returns the method to use.
	 */
	@Override
	public String getMethod() {
		return "POST";
	}
}
