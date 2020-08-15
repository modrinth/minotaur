# Diluv-Gradle
A Gradle plugin for uploading build artifacts directly to Diluv.

## Usage Guide
To use this plugin you must add it to your build script classpath. This will make the code available to you in your script file.

**Note:** This plugin has only been tested with Gradle 4.9. While compatibility with other versions is highly likely we make no guarantees.    
**Note:** This plugin is still being developed and is not yet on maven central.    

```groovy
buildscript {
    repositories {        
        mavenCentral()
    }
    dependencies {
        classpath 'com.diluv.diluvgradle:DiluvGradle:VERSION'
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

| Property       | Required | Description                                                                      |
|----------------|----------|----------------------------------------------------------------------------------|
| apiURL         | false    | The API endpoint URL to use for uploading files. Defaults to official Diluv API. |
| token          | true     | A valid API token for the Diluv API.                                             |
| projectId      | true     | The ID of the project to upload to.                                              |
| projectVersion | true     | The version of the file. Please use semantic versioning.                         |
| changelog      | false    | The changelog for the file. Allows markdown formatting.                          |
| uploadFile     | true     | The file to upload. Can be an actual file or a file task.                        |
| releaseType    | false    | The release status of the file. Defaults to "alpha".                             |
| classifier     | false    | The type of file being uploaded. Defaults to binary.                             |
| gameVersion    | true     | The version of the game the file is for.                                         |
| dependencies   | false    | Currently unimplemented.                                                         |

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
    }
}
```
