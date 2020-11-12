# [Diluv-Gradle](https://plugins.gradle.org/plugin/com.diluv.diluvgradle)
A Gradle plugin for uploading build artifacts directly to Diluv.

## Usage Guide
To use this plugin you must add it to your build script. This can be done using the plugins DSL or added to the classpath directly for legacy scripts.

**Plugin DSL**    
```gradle
plugins {
    id "com.diluv.diluvgradle" version "1.2.2"
}
```

**Legacy**
```gradle
buildscript {
    repositories {
        maven {
            url "https://plugins.gradle.org/m2/"
        }
    }
    dependencies {
        classpath group: 'gradle.plugin.com.diluv.diluvgradle', name: 'DiluvGradle', version: '1.2.2'
    }
}
```

The next step is to create a new task for uploading to Diluv. This task allows you to configure the upload and control when and how files are uploaded.

```groovy
import com.diluv.diluvgradle.TaskDiluvUpload

task publishDiluv (type: TaskDiluvUpload){

    token = 'a7104dd8-f43a-4468-b5cd-b6ed3394916d' // Use an environment property!
    projectId = 123456
    projectVersion = '1.0.0'
    uploadFile = jar // This is the java jar task
    gameVersion = '1.12.2'
}
```

### Available Properties

| Property                         | Required | Description                                                                         |
|----------------------------------|----------|-------------------------------------------------------------------------------------|
| apiURL                           | false    | The API endpoint URL to use for uploading files. Defaults to official Diluv API.    |
| token                            | true     | A valid API token for the Diluv API.                                                |
| projectId                        | true     | The ID of the project to upload to.                                                 |
| projectVersion                   | true     | The version of the file. Please use semantic versioning.                            |
| changelog                        | false    | The changelog for the file. Allows Markdown formatting.                             |
| uploadFile                       | true     | The file to upload. Can be an actual file or a file task.                           |
| releaseType                      | false    | The release status of the file. Defaults to "alpha".                                |
| classifier                       | false    | The type of file being uploaded. Defaults to binary.                                |
| gameVersion                      | true     | The version of the game the file is for. Comma separated for multiple.              |
| failSilently                     | false    | When true an upload failure will not fail your build.                               |
| addDependency(projectId)         | false    | Marks a project as a required dependency.                                           |
| addOptionalDependency(projectId) | false    | Marks a project as an optional/soft dependency.                                     |
| addIncompatibility(projectId)    | false    | Marks a project as being incompatible with this file.                               |
| addRelation(projectId, type)     | false    | Adds a project relationship to the file. Only some relationship types are accepted. |

**Note:** In some scenarios the `gameVersion` property can be detected automatically. For example the ForgeGradle and LoomGradle environments. For best results you should set this property manually.

### Additional Properties

| Name                  | Description                                                                                         |
|-----------------------|-----------------------------------------------------------------------------------------------------|
| uploadInfo            | The response from the API server. Only present after upload is completed successfully.              |
| errorInfo             | The response from the API server. Only present after an upload fails.                               |
| wasUploadSuccessful() | Checks if the upload was successful or not. Should be used before accessing uploadInfo or errorInfo |

#### Upload Info

| Property            | Type        | Description                                             |
|---------------------|-------------|---------------------------------------------------------|
| status              | String      | The current status of the file. Approved, pending, etc. |
| lastStatusChanged   | Long        | The time the status last changed.                       |
| id                  | Long        | The ID for the uploaded file.                           |
| name                | String      | The name of the uploaded file.                          |
| downloadURL         | String      | A download URL for the uploaded file.                   |
| size                | Long        | The file size in bytes.                                 |
| changelog           | String      | The changelog for the file.                             |
| sha512              | String      | A sha512 hash of the file.                              |
| releaseType         | String      | The release type of the file.                           |
| classifier          | String      | The classifier of the file.                             |
| createdAt           | Long        | When the file was created.                              |
| gameVersions        | GameVersion | Not yet implemented.                                    |
| gameSlug            | String      | The slug for the game that the project belongs to.      |
| projectTypeSlug     | String      | The slug for the project type.                          |
| projectSlug         | String      | The slug of the project.                                |
| uploaderUserId      | Long        | The ID of the user who uploaded the file.               |
| uploaderUsername    | String      | The username of the user who uploaded the file.         |
| uploaderDisplayName | String      | The display name of the user who uploaded the file.     |

#### Error Info

| Property | Type   | Description                                                          |
|----------|--------|----------------------------------------------------------------------|
| type     | String | The type of error that occurred, for example an authorization error. |
| error    | String | An API error code string.                                            |
| message  | String | The error message from the API.                                      |

## Development Information
This section contains information useful to those working on the plugin directly or creating their own custom versions of our plugin. If you want to just use DiluvGradle in your build pipeline you will not need to know or understand any of this.

### Local Usage
If you want to use the plugin from your local maven repo make sure you have added the mavenLocal repository to your script. Grabbing the plugin is the same as normal. To publish locally you run `./gradlew clean build publishToMavenLocal`. Local maven files can be found in the `%home%/.m2/` directory.

### Direct File Usage
In some cases you may want to use the JAR file directly in your script rather than pulling from a repository. This is generally not recommended but may be unavoidable in some circumstances. If you do this make sure you add all of our dependencies to your classpath. Using the file directly will not use our pom file and will not pull these dependencies in for you.

```groovy
buildscript {
    repositories {
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath files { file('../../../../build/libs').listFiles()}
        classpath group: 'org.apache.httpcomponents', name: 'httpmime', version: '4.5.2'
        classpath group: 'org.apache.httpcomponents', name: 'httpclient', version: '4.5.2'
        classpath group: 'com.google.code.gson', name: 'gson', version: '2.6.2'
    }
}
```
