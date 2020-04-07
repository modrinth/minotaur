# Diluv-Gradle

A Gradle plugin for uploading build artifacts and other files to Diluv.

## Requirements

This plugin has only been tested with Gradle 4.9. Support for older Gradle versions is not guaranteed.

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
        classpath 'com.diluv.diluvgradle:DiluvGradle:0.0.0'
    }
}
```

The next step is to create a new task for uploading to Diluv. This task allows you to configure the upload and control when and how files are uploaded.

```groovy
import com.diluv.diluvgradle.TaskDiluvUpload

task publishDiluv (type: TaskDiluvUpload){
    
    token = 'a7104dd8-f43a-4468-b5cd-b6ed3394916d' // Use a property or something!
    projectId = 242195
    uploadFile = jar // This is the java jar task
    changelog = 'The changelog for my file.'
}
```

Now you will be able to upload your build by adding publishDiluv to the Gradle CLI. 
```
./gradlew build publishDiluv
```