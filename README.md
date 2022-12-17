# [Minotaur](https://plugins.gradle.org/plugin/com.modrinth.minotaur)

A Gradle plugin for interfacing directly with Modrinth, through uploading build artifacts and syncing project bodies.

Want to use a GitHub Action instead of a Gradle plugin? Check out [Kir-Antipov/mc-publish](https://github.com/Kir-Antipov/mc-publish), but note that Modrinth does not give support for `mc-publish` where we do for Minotaur.

Still using Minotaur v1? You should switch as soon as possible, because it uses the deprecated API v1. Migration instructions can be found [on the Modrinth documentation page](https://docs.modrinth.com/docs/migrations/v1-to-v2/#minotaur-v1-to-v2).

## Usage Guide

To use this plugin you must add it to your Gradle build script. After that, you can use the `modrinth` task to upload the version to Modrinth.

### Groovy

<details open="open"><summary>Groovy DSL</summary>

```groovy
// build.gradle
plugins {
    id "com.modrinth.minotaur" version "2.+"
    // It's safest to have this on 2.+ to get the latest features and
    // bug fixes without having to worry about breaking changes.
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
// build.gradle
modrinth {
    token = System.getenv("MODRINTH_TOKEN") // This is the default. Remember to have the MODRINTH_TOKEN environment variable set or else this will fail, or set it to whatever you want - just make sure it stays private!
    projectId = "my-project" // This can be the project ID or the slug. Either will work!
    versionNumber = "1.0.0" // You don't need to set this manually. Will fail if Modrinth has this version already
    versionType = "release" // This is the default -- can also be `beta` or `alpha`
    uploadFile = jar // With Loom, this MUST be set to `remapJar` instead of `jar`!
    gameVersions = ["1.18", "1.18.1"] // Must be an array, even with only one version
    loaders = ["fabric"] // Must also be an array - no need to specify this if you're using Loom or ForgeGradle
    dependencies { // A special DSL for creating dependencies
        // scope.type
        // The scope can be `required`, `optional`, `incompatible`, or `embedded`
        // The type can either be `project` or `version`
        required.project "fabric-api" // Creates a new required dependency on Fabric API
    }
}
```

</details>

### Kotlin

<details><summary>Kotlin DSL</summary>

```kotlin
// build.gradle.kts
plugins {
    id("com.modrinth.minotaur") version "2.+"
    // It's safest to have this on 2.+ to get the latest features and
    // bug fixes without having to worry about breaking changes.
}


// settings.gradle.kts
// This is probably already present.
pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}
```

The next step is to configure the task for uploading to Modrinth. This allows you to configure the upload and control when and how versions are uploaded.

```kotlin
// build.gradle.kts
modrinth {
    token.set(System.getenv("MODRINTH_TOKEN")) // This is the default. Remember to have the MODRINTH_TOKEN environment variable set or else this will fail, or set it to whatever you want - just make sure it stays private!
    projectId.set("my-project") // This can be the project ID or the slug. Either will work!
    versionNumber.set("1.0.0") // You don't need to set this manually. Will fail if Modrinth has this version already
    versionType.set("release") // This is the default -- can also be `beta` or `alpha`
    uploadFile.set(tasks.jar) // With Loom, this MUST be set to `remapJar` instead of `jar`!
    gameVersions.addAll(arrayOf("1.18", "1.18.1")) // Must be an array, even with only one version
    loaders.add("fabric") // Must also be an array - no need to specify this if you're using Loom or ForgeGradle
    dependencies { // A special DSL for creating dependencies
        // scope.type
        // The scope can be `required`, `optional`, `incompatible`, or `embedded`
        // The type can either be `project` or `version`
        required.project("fabric-api") // Creates a new required dependency on Fabric API
    }
}
```

</details>

### Syncing Project Body

In tandem with the `syncBodyFrom` property in your `modrinth {...}` block, you may set up syncing between, for example, your project's `README.md` and your project's body on Modrinth.

For example:
```groovy
// build.gradle
modrinth {
    // ...
    syncBodyFrom = rootProject.file("README.md").text
}
```

This will sync the contents of the `README.md` file in your project's root to your project.

If you have some things you want in your `README.md` but not in your Modrinth project body, you may also add comments to the file. Anything between `<!-- modrinth_exclude.start -->` and `<!-- modrinth_exclude.end -->` will be excluded.

This does not occur with the `modrinth` task; you must use the `modrinthSyncBody` task separately to accomplish this. You can make sure the project body gets synced with every publish by making the `modrinthSyncBody` task depend on `modrinth`:

```groovy
tasks.modrinth.dependsOn(tasks.modrinthSyncBody)
```

Be careful with this task! Once a body is changed, you **cannot** get it back. You can use `debugMode` to make sure that what's to be uploaded is what you want.

### Available Properties

The following properties can be set within the `modrinth {...}` block.

| Property         | Required | Description                                                               | Default                                      |
|------------------|----------|---------------------------------------------------------------------------|----------------------------------------------|
| apiURL           | false    | The API endpoint URL to use for uploading files.                          | `https://api.modrinth.com/v2`                |
| token            | false    | A valid API token for the Modrinth API.                                   | `MODRINTH_TOKEN` environment variable        |
| projectId        | true     | The ID of the project to upload to.                                       |                                              |
| versionNumber    | false    | The version number of the version.                                        | `version` declaration                        |
| versionName      | false    | The name of the version.                                                  | `versionNumber`                              |
| changelog        | false    | The changelog for the file. Allows Markdown formatting.                   | `No changelog was specified.`                |
| uploadFile       | true     | The file to upload. Can be an actual file or a file task.                 |                                              |
| additionalFiles  | false    | An array of additional files to be uploaded to a version.                 |                                              |
| versionType      | false    | The stability level of the version. Can be `release`, `beta`, or `alpha`. | `release`                                    |
| gameVersions     | true     | An array of game versions that this version supports.                     | Detected based on the Gradle plugins you use |
| loaders          | false    | An array of mod loaders that this version supports.                       | Detected based on the Gradle plugins you use |
| dependencies     | false    | Dependencies of the uploaded version.                                     |                                              |
| failSilently     | false    | When true an upload failure will not fail your build.                     | `false`                                      |
| detectLoaders    | false    | Whether mod loaders will be automatically detected.                       | `true`                                       |
| autoAddDependsOn | false    | Whether to automatically add task dependencies from upload files.         | `true`                                       |
| debugMode        | false    | Doesn't actually upload the version, and prints the data to be uploaded.  | `false`                                      |
| syncBodyFrom     | false    | The text to sync the body from in the `modrinthSyncBody` task.            |                                              |

**Note:** In most scenarios the `gameVersions` and `loaders` properties can be detected automatically. This is done in environments using ForgeGradle and Fabric Loom.

### Additional Properties

The following properties can only be accessed through `tasks.modrinth.<property>`.

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
| versionType   | VersionType | The type of the release - `alpha`, `beta`, or `release`.               |
| files         | List        | A list of files available for download for this version.               |
| gameVersions  | List        | A list of versions of Minecraft that this version of the mod supports. |
| loaders       | List        | The loaders that this version works on                                 |
| dependency    | Dependency  | A list of mods that this version depends on.                           |

#### Error Info

| Property    | Type   | Description                                                          |
|-------------|--------|----------------------------------------------------------------------|
| error       | String | The type of error that occurred, for example an authorization error. |
| description | String | The error message from the API.                                      |

## Development Information

For contributing information, please see the Minotaur section of the [Modrinth contributing guide](https://docs.modrinth.com/docs/details/contributing/#minotaur-gradle-plugin).
