# jacoco-android-gradle-plugin
[![Build Status](https://travis-ci.org/arturdm/jacoco-android-gradle-plugin.svg)](https://travis-ci.org/arturdm/jacoco-android-gradle-plugin)
[![codecov.io](http://codecov.io/github/arturdm/jacoco-android-gradle-plugin/coverage.svg?branch=master)](http://codecov.io/github/arturdm/jacoco-android-gradle-plugin?branch=master)
[![Download](https://api.bintray.com/packages/dicedmelon/maven/com.dicedmelon.gradle:jacoco-android/images/download.svg)](https://bintray.com/dicedmelon/maven/com.dicedmelon.gradle:jacoco-android/_latestVersion)

A Gradle plugin that adds fully configured `JacocoReport` tasks for each Android application and library project variant.

## Why

The main purpose of this plugin is to automate the process of providing `JacocoReport` tasks configuration to Android projects.

## Usage

```groovy
buildscript {
  repositories {
    ...
    maven { url 'https://dl.bintray.com/dicedmelon/maven' }
  }
  dependencies {
    ...
    classpath 'com.dicedmelon.gradle:jacoco-android:0.1.0'
  }
}

apply plugin: 'com.android.application'
apply plugin: 'jacoco-android'

android {
  ...
  productFlavors {
    free {}
    paid {}
  }
}

jacocoAndroidUnitTestReport {
  excludes = ['**/R.class',
              '**/R$*.class',
              '**/BuildConfig.*',
              '**/Manifest*.*']
}
```

This configuration will create a `JacocoReport` task for each variant and an additional `jacocoTestReport` task that runs all of them.
```
jacocoTestPaidDebugUnitTestReport
jacocoTestFreeDebugUnitTestReport
jacocoTestPaidReleaseUnitTestReport
jacocoTestFreeReleaseUnitTestReport
jacocoTestReport
```

To generate reports run:

```shell
$ ./gradlew jacocoTestReport
```
