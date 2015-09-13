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
    jcenter()
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

## Examples
* https://github.com/devinciltd/lib

## License
```
Copyright 2015 Artur Stępniewski

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
