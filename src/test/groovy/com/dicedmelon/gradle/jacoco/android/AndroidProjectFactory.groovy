package com.dicedmelon.gradle.jacoco.android

import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

public class AndroidProjectFactory {

  public static final File PROJECT_SOURCE_DIR = new File("src/test/resources");

  static Project create() {
    def project = ProjectBuilder.builder().build();
    FileUtils.copyDirectory(PROJECT_SOURCE_DIR, project.projectDir)
    project
  }

  static void configureAsLibraryAndApplyPlugin(Project project) {
    project.apply plugin: 'com.android.library'
    configure(project)
    project.apply plugin: JacocoAndroidPlugin
  }

  static void configure(Project project) {
    project.android {
      compileSdkVersion 23
      buildToolsVersion '23.0.0'

      defaultConfig {
        versionCode 1
        versionName '1.0'
        minSdkVersion 23
        targetSdkVersion 23
      }

      productFlavors {
        free {}
        paid {}
      }
    }
  }
}
