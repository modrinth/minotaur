# [Minotaur](https://plugins.gradle.org/plugin/com.modrinth.minotaur)

A Gradle plugin for uploading build artifacts directly to Modrinth.

## Usage Guide

To use this plugin you must add it to your Gradle build script.

### Groovy

<details open="open"><summary>Groovy DSL</summary>

```groovy
// build.gradle
plugins {
    id "com.modrinth.minotaur" version "2.+"
}

// settings.gradle
// This is probably already present.
pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}
```

The next step is to configure the task for uploading to Modrinth. This allows you to configure the upload and control when and how versions are uploaded.

```groovy
import com.modrinth.minotaur.dependencies.ModDependency
modrinth {
    token = System.getenv("MODRINTH_TOKEN") // This is the default. Remember to have the MODRINTH_TOKEN environment variable set or else this will fail, or set it to whatever you want - just make sure it stays private!
    projectId = "AABBCCDD"
    versionNumber = "1.0.0" // You don't need to set this manually. Will fail if Modrinth has this version already
    versionType = "release" // This is the default
    uploadFile = jar // With Fabric Loom or Architectury Loom, this MUST be set to `remapJar` instead of `jar`!
    gameVersions = ["1.18", "1.18.1"] // Must be an array, even with only one version
    loaders = ["fabric"] // Must also be an array - no need to specify this if you're using Fabric Loom or ForgeGradle
    dependencies = [ // Yet another array. Create a new `ModDependency` or `VersionDependency` with two strings - the ID and the scope
            new ModDependency("P7dR8mSH", "required") // Creates a new required dependency on Fabric API
    ]
}
```

</details>

### Kotlin

<details><summary>Kotlin DSL</summary>

```kotlin
// build.gradle.kts
plugins {
    id("com.modrinth.minotaur") version "2.+"
}


// settings.gradle.kts
// This is probably already present.
pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}
```

The next step is to create a new task for uploading to Modrinth. This task allows you to configure the upload and control when and how versions are uploaded.

```kotlin
import com.modrinth.minotaur.dependencies.ModDependency
modrinth {
    token = System.getenv("MODRINTH_TOKEN") // This is the default. Remember to have the MODRINTH_TOKEN environment variable set or else this will fail, or set it to whatever you want - just make sure it stays private!
    projectId = "AABBCCDD"
    versionNumber = "1.0.0" // You don't need to set this manually. Will fail if Modrinth has this version already
    versionType = "release" // This is the default
    uploadFile = jar // With Fabric Loom or Architectury Loom, this MUST be set to `remapJar` instead of `jar`!
    gameVersions = arrayOf("1.18", "1.18.1") // Must be an array, even with only one version
    loaders = arrayOf("fabric") // Must also be an array - no need to specify this if you're using Fabric Loom or ForgeGradle
    dependencies = arrayOf( // Yet another array. Create a new `ModDependency` or `VersionDependency` with two strings - the ID and the scope
        ModDependency("P7dR8mSH", "required") // Creates a new required dependency on Fabric API
    )
}
```

</details>

### Available Properties

| Property        | Required | Description                                                               | Default                                                            |
|-----------------|----------|---------------------------------------------------------------------------|--------------------------------------------------------------------|
| apiURL          | false    | The API endpoint URL to use for uploading files.                          | `https://api.modrinth.com/v2`                                      |
| token           | false    | A valid API token for the Modrinth API.                                   | `MODRINTH_TOKEN` environment variable                              |
| projectId       | true     | The ID of the project to upload to.                                       |                                                                    |
| versionNumber   | false    | The version number of the version.                                        | `version` declaration                                              |
| versionName     | false    | The name of the version.                                                  | `versionNumber`                                                    |
| changelog       | false    | The changelog for the file. Allows Markdown formatting.                   | `No changelog was specified.`                                      |
| uploadFile      | true     | The file to upload. Can be an actual file or a file task.                 |                                                                    |
| additionalFiles | false    | An array of additional files to be uploaded to a version.                 |                                                                    |
| versionType     | false    | The stability level of the version. Can be `release`, `beta`, or `alpha`. | `release`                                                          |
| gameVersions    | true     | An array of game versions that this version supports.                     | `MC_VERSION` on FG, `MinecraftProvider.minecraftVersion()` on Loom |
| loaders         | false    | An array of mod loaders that this version supports.                       | `forge` if using FG, `fabric` if using Loom                        |
| dependencies    | false    | Dependencies of the uploaded version.                                     |                                                                    |
| failSilently    | false    | When true an upload failure will not fail your build.                     | `false`                                                            |
| detectLoaders   | false    | Whether mod loaders will be automatically detected.                       | `true`                                                             |

**Note:** In most scenarios the `gameVersions` and `loaders` properties can be detected automatically. This is done in environments using ForgeGradle and Fabric Loom.

## Development Information

This section contains information useful to those working on the plugin directly or creating their own custom versions of our plugin. If you want to just use Minotaur in your build pipeline you will not need to know or understand any of this.

### Local Usage

If you want to use the plugin from your local maven repo make sure you have added the mavenLocal repository to your script. Grabbing the plugin is the same as normal. To publish locally you run `./gradlew clean build publishToMavenLocal`. Local maven files can be found in the `%home%/.m2/` directory.

### Additional Properties

These will only be accessible if you're creating your own `TaskModrinthUpload`.

| Name                  | Description                                                                                         |
|-----------------------|-----------------------------------------------------------------------------------------------------|
| uploadInfo            | The response from the API server. Only present after upload is completed successfully.              |
| errorInfo             | The response from the API server. Only present after an upload fails.                               |
| wasUploadSuccessful() | Checks if the upload was successful or not. Should be used before accessing uploadInfo or errorInfo |

#### Upload Info

| Property      | Type        | Description                                                            |
|---------------|-------------|------------------------------------------------------------------------|
| id            | String      | The ID for the uploaded version.                                       |
| projectId     | String      | The ID of the mod this version is for.                                 |
| authorId      | String      | The ID of the author who published this version                        |
| featured      | Boolean     | Whether the version is featured or not                                 |
| name          | String      | The name of this version                                               |
| versionNumber | String      | The version number. Ideally will follow semantic versioning            |
| changelog     | String      | The changelog for this version of the mod.                             |
| datePublished | Date        | The date that this version was published.                              |
| downloads     | Integer     | The number of downloads this specific version has had.                 |
| versionType   | VersionType | The type of the release - `ALPHA`, `BETA`, or `RELEASE`.               |
| files         | List        | A list of files available for download for this version.               |
| gameVersions  | List        | A list of versions of Minecraft that this version of the mod supports. |
| loaders       | List        | The loaders that this version works on                                 |
| dependency    | Dependency  | A list of mods that this version depends on.                           |

#### Error Info

| Property    | Type   | Description                                                          |
|-------------|--------|----------------------------------------------------------------------|
| error       | String | The type of error that occurred, for example an authorization error. |
| description | String | The error message from the API.                                      |
