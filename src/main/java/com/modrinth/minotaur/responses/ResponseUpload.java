package com.modrinth.minotaur.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.modrinth.minotaur.dependencies.Dependency;
import com.modrinth.minotaur.request.VersionType;
import masecla.modrinth4j.model.version.ProjectVersion;
import masecla.modrinth4j.model.version.ProjectVersion.ProjectFile;
import org.jetbrains.annotations.ApiStatus;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class defines a POJO that represents the API response for versions that have been successfully uploaded to
 * Modrinth.
 * @deprecated Please use {@link ProjectVersion}
 */
@SuppressWarnings("ALL")
@Deprecated
public class ResponseUpload {
    /**
     * The ID of the version, encoded as a base62 string.
     */
    @Expose
    @SerializedName("id")
    private String id;

    /**
     * The ID of the mod this version is for.
     */
    @Expose
    @SerializedName("project_id")
    private String projectId;

    /**
     * The ID of the author who published this version
     */
    @Expose
    @SerializedName("author_id")
    private String authorId;

    /**
     * Whether the version is featured or not
     */
    @Expose
    @SerializedName("featured")
    private boolean featured;

    /**
     * The name of this version
     */
    @Expose
    @SerializedName("name")
    private String name;

    /**
     * The version number. Ideally will follow semantic versioning
     */
    @Expose
    @SerializedName("version_number")
    private String versionNumber;

    /**
     * The changelog for this version of the mod.
     */
    @Expose
    @SerializedName("changelog")
    private String changelog;

    /**
     * The date that this version was published.
     */
    @Expose
    @SerializedName("date_published")
    private Date datePublished;

    /**
     * The number of downloads this specific version has had.
     */
    @Expose
    @SerializedName("downloads")
    private int downloads;

    /**
     * The type of the release - `Alpha`, `Beta`, or `Release`.
     */
    @Expose
    @SerializedName("version_type")
    private VersionType versionType;

    /**
     * A list of files available for download for this version.
     */
    @Expose
    @SerializedName("files")
    private Collection<VersionFile> files = new ArrayList<>();

    /**
     * A list of versions of Minecraft that this version of the mod supports.
     */
    @Expose
    @SerializedName("game_versions")
    private Collection<String> gameVersions = new ArrayList<>();

    /**
     * The loaders that this version works on
     */
    @Expose
    @SerializedName("loaders")
    private Collection<String> loaders = new ArrayList<>();

    /**
     * A list of mods that this version depends on.
     */
    @Expose
    @SerializedName("dependencies")
    private Collection<Dependency> dependencies = new ArrayList<>();

    /**
     * @return {@link #id}
     */
    public String getId() {
        return this.id;
    }

    /**
     * @return {@link #projectId}
     */
    public String getProjectId() {
        return this.projectId;
    }

    /**
     * @return {@link #authorId}
     */
    public String getAuthorId() {
        return this.authorId;
    }

    /**
     * @return {@link #featured}
     */
    public boolean isFeatured() {
        return this.featured;
    }

    /**
     * @return {@link #name}
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return {@link #versionNumber}
     */
    public String getVersionNumber() {
        return this.versionNumber;
    }

    /**
     * @return {@link #changelog}
     */
    public String getChangelog() {
        return this.changelog;
    }

    /**
     * @return {@link #datePublished}
     */
    public Date getDatePublished() {
        return this.datePublished;
    }

    /**
     * @return {@link #downloads}
     */
    public int getDownloads() {
        return this.downloads;
    }

    /**
     * @return {@link #versionType}
     */
    public VersionType getVersionType() {
        return this.versionType;
    }

    /**
     * @return {@link #files}
     */
    public Collection<VersionFile> getFiles() {
        return this.files;
    }

    /**
     * @return {@link #gameVersions}
     */
    public Collection<String> getGameVersions() {
        return this.gameVersions;
    }

    /**
     * @return {@link #loaders}
     */
    public Collection<String> getLoaders() {
        return this.loaders;
    }

    /**
     * @return {@link #dependencies}
     */
    public Collection<Dependency> getDependencies() {
        return this.dependencies;
    }

    /**
     * Constructs a {@link ResponseUpload} from a {@link ProjectVersion}
     * @param newVersion {@link ProjectVersion} instance
     */
    @ApiStatus.Internal
    public ResponseUpload(ProjectVersion newVersion) {
        this.name = newVersion.getName();
        this.versionNumber = newVersion.getVersionNumber();
        this.changelog = newVersion.getChangelog();
        this.dependencies =
            Arrays.stream(newVersion.getDependencies()).map(Dependency::fromNew).collect(Collectors.toList());
        this.gameVersions = Arrays.asList(newVersion.getGameVersions());
        this.versionType = VersionType.valueOf(newVersion.getVersionType().name());
        this.loaders = Arrays.asList(newVersion.getLoaders());
        this.featured = newVersion.isFeatured();
        this.id = newVersion.getId();
        this.projectId = newVersion.getProjectId();
        this.authorId = newVersion.getAuthorId();
        this.datePublished = Date.from(Instant.parse(newVersion.getDatePublished()));
        this.downloads = newVersion.getDownloads();
        this.files =
            Arrays.stream(newVersion.getFiles()).map(VersionFile::fromNew).collect(Collectors.toList());
    }

    /**
     * A single version file.
     */
    public static class VersionFile {
        /**
         * A map of hashes of the file. The key is the hashing algorithm and the value is the string version of the
         * hash.
         */
        @Expose
        @SerializedName("hashes")
        private Map<String, String> hashes;

        /**
         * A direct link to the file for downloading it.
         */
        @Expose
        @SerializedName("url")
        private String url;

        /**
         * The filename of the file.
         */
        @Expose
        @SerializedName("filename")
        private String filename;

        /**
         * Whether the file is the primary file of the version.
         */
        @Expose
        @SerializedName("primary")
        private boolean primary;

        /**
         * @return {@link #hashes}
         */
        public Map<String, String> getHashes() {
            return this.hashes;
        }

        /**
         * @return {@link #url}
         */
        public String getUrl() {
            return this.url;
        }

        /**
         * @return {@link #filename}
         */
        public String getFilename() {
            return this.filename;
        }

        /**
         * @return {@link #primary}
         */
        public boolean isPrimary() {
            return this.primary;
        }

        /**
         * @param newFile the {@link ProjectFile} to convert to a {@link VersionFile}
         * @return a converted {@link VersionFile}
         */
        public static VersionFile fromNew(ProjectFile newFile) {
            VersionFile file = new VersionFile();
            file.hashes = new HashMap<>();
            file.hashes.put("sha1", newFile.getHashes().getSha1());
            file.hashes.put("sha512", newFile.getHashes().getSha512());
            file.url = newFile.getUrl();
            file.filename = newFile.getFilename();
            file.primary = newFile.isPrimary();
            return file;
        }
    }
}
