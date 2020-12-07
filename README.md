# [Minotaur](https://plugins.gradle.org/plugin/com.modrinth.minotaur)
A Gradle plugin for uploading build artifacts directly to Modrinth.

## Usage Guide
To use this plugin you must add it to your build script. This can be done using the plugins DSL or added to the classpath directly for legacy scripts.

**Plugin DSL**    
```gradle
plugins {
    id "com.modrinth.minotaur" version "1.0.0"
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
        classpath group: 'gradle.plugin.com.modrinth.minotaur', name: 'Minotaur', version: '1.0.0'
    }
}
```

The next step is to create a new task for uploading to Modrinth. This task allows you to configure the upload and control when and how versions are uploaded.

```groovy
import TaskModrinthUpload

task publishModrinth (type: TaskModrinthUpload){

    token = 'secret' // Use an environment property!
    projectId = 'ssUbhMkL'
    versionNumber = '1.0.0'
    uploadFile = jar // This is the java jar task
    addLoader('1.16.2')
    addLoader('fabric')
}
```

### Available Properties

| Property                         | Required | Description                                                                         |
|----------------------------------|----------|-------------------------------------------------------------------------------------|
| apiURL                           | false    | The API endpoint URL to use for uploading files. Defaults to official Modrinth API. |
| token                            | true     | A valid API token for the Modrinth API.                                             |
| projectId                        | true     | The ID of the project to upload to.                                                 |
| versionNumber                    | true     | The version number of the version.                                                  |
| versionName                      | false    | The name of the version.                                                            |
| changelog                        | false    | The changelog for the file. Allows Markdown formatting.                             |
| uploadFile                       | true     | The file to upload. Can be an actual file or a file task.                           |
| releaseType                      | false    | The release status of the file. Defaults to "alpha".                                |
| failSilently                     | false    | When true an upload failure will not fail your build.                               |
| addGameVersion(version)          | true     | Adds a game version that this file supports. At least one is needed.                |
| addLoader(loader)                | true     | Allows supported mod loaders to be specified for the file.                           |
| addFile(file)                    | false    | Allows supported mod loaders to be specified for the file.                          |

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
| id                  | String        | The ID for the uploaded version.                      |

More properties coming soon!

#### Error Info

| Property | Type   | Description                                                          |
|----------|--------|----------------------------------------------------------------------|
| error    | String | The type of error that occurred, for example an authorization error. |                                           |
| description  | String | The error message from the API.                                  |

## Development Information
This section contains information useful to those working on the plugin directly or creating their own custom versions of our plugin. If you want to just use Minotaur in your build pipeline you will not need to know or understand any of this.

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
