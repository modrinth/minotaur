# Diluv-Gradle

A Gradle plugin that allows files and build artifacts to be uploaded to Diluv automatically.

## Requirements

This plugin has only been tested with Gradle 4.9. Compatibility with other versions is not guranteed.

**NOTE:** In some environments you will need to add httpmime and httpclient to the script classpath. This is still being investigated.

```groovy
buildscript {
    repositories {
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath group: 'org.apache.httpcomponents', name: 'httpmime', version: '4.5.2'
        classpath group: 'org.apache.httpcomponents', name: 'httpclient', version: '4.5.2'
    }
}
```

## Setup Guide

To use this plugin you must add it to the classpath of your build script. This makes the plugin's code available to your script.

```groovy
buildscript {
    repositories {        
        maven {
          url 'https://plugins.gradle.org/m2/'
        }
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
| dependencies   | false    | Currently unknown.                                                               |