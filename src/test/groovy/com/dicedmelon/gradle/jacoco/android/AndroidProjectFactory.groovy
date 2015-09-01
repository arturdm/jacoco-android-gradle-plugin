package com.dicedmelon.gradle.jacoco.android

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

public class AndroidProjectFactory {

  public static final File RESOURCE_WORKING_DIR = new File("src/test/resources");

  static Project library() {
    Project project = ProjectBuilder.builder().withProjectDir(RESOURCE_WORKING_DIR).build();
    project.apply plugin: 'com.android.library'
    project.android {
      compileSdkVersion 23
      buildToolsVersion '23.0.0'

      defaultConfig {
        versionCode 1
        versionName '1.0'
        minSdkVersion 23
        targetSdkVersion 23
      }

      buildTypes {
        release {
          signingConfig signingConfigs.debug
        }
      }

      productFlavors {
        free {

        }
        paid {

        }
      }
    }

    return project
  }
}
