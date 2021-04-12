package com.modrinth.minotaur.responses;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.modrinth.minotaur.request.Dependency;
import com.modrinth.minotaur.request.VersionType;

/**
 * This class defines a POJO that represents the API response for versions that have been
 * successfully uploaded to Modrinth.
 */
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
    @SerializedName("mod_id")
    private String modId;

    /**
     * The ID of the author who published this version
     */
    @Expose
    @SerializedName("author_id")
    private String authorId;

    /**
     *  Whether the version is featured or not
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
     *  The type of the release - `Alpha`, `Beta`, or `Release`.
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

    public String getId () {
        return this.id;
    }

    public String getModId() {
        return this.modId;
    }

    public String getAuthorId() {
        return this.authorId;
    }

    public boolean isFeatured() {
        return this.featured;
    }

    public String getName() {
        return this.name;
    }

    public String getVersionNumber() {
        return this.versionNumber;
    }

    public String getChangelog() {
        return this.changelog;
    }

    public Date getDatePublished() {
        return this.datePublished;
    }

    public int getDownloads() {
        return this.downloads;
    }

    public VersionType getVersionType() {
        return this.versionType;
    }

    public Collection<VersionFile> getFiles() {
        return this.files;
    }

    public Collection<String> getGameVersions() {
        return this.gameVersions;
    }

    public Collection<String> getLoaders() {
        return this.loaders;
    }

    public Collection<Dependency> getDependencies() {
        return this.dependencies;
    }

    /**
     * A single mod file, with a url for the file and the file's hash
     */
    public class VersionFile {
        /**
         * A map of hashes of the file.  The key is the hashing algorithm
         * and the value is the string version of the hash.
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
         * Whether the file is the primary file of a version
         */
        @Expose
        @SerializedName("primary")
        private boolean primary;

        public Map<String, String> getHashes() {
            return this.hashes;
        }

        public String getUrl() {
            return this.url;
        }

        public String getFilename() {
            return this.filename;
        }

        public boolean isPrimary() {
            return this.primary;
        }
    }
}