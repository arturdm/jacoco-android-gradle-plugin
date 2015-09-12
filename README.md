[![Build Status](https://travis-ci.org/arturdm/jacoco-android-gradle-plugin.svg)](https://travis-ci.org/arturdm/jacoco-android-gradle-plugin)

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
