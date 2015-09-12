[![Build Status](https://travis-ci.org/arturdm/jacoco-android-gradle-plugin.svg)](https://travis-ci.org/arturdm/jacoco-android-gradle-plugin)
[![codecov.io](http://codecov.io/github/arturdm/jacoco-android-gradle-plugin/coverage.svg?branch=master)](http://codecov.io/github/arturdm/jacoco-android-gradle-plugin?branch=master)

# jacoco-android-gradle-plugin

```groovy
buildscript {
  repositories {
    jcenter()
  }
  dependencies {
    ...
    classpath 'com.dicedmelon.gradle:jacoco-android:0.1.0'
  }
}

apply plugin: 'com.android.application'
apply plugin: 'jacoco-android'

jacocoAndroidUnitTestReport {
  excludes = ['**/R.class',
              '**/R$*.class',
              '**/BuildConfig.*',
              '**/Manifest*.*']
}
```

```shell
$ ./gradlew jacocoTestReport
```
