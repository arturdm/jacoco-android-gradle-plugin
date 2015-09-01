package com.dicedmelon.gradle.jacoco.android

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.internal.project.ProjectInternal

class JacocoAndroidPlugin implements Plugin<ProjectInternal> {

  @Override public void apply(ProjectInternal project) {
    def plugin = project.plugins.findPlugin('android') ?:
        project.plugins.findPlugin('android-library')
    if (!plugin) {
      throw new GradleException(
          'You must apply the Android plugin or the Android library plugin before using the jacoco-android plugin')
    }

    def isLibraryPlugin = plugin.class.name.endsWith('.LibraryPlugin')

    def variants
    if (isLibraryPlugin) {
      variants = "libraryVariants"
    } else {
      variants = "applicationVariants"
    }

    project.android[variants].all {
      println it.name
    }

  }

  def sourceDirs() {

  }

  def classesDir() {

  }

  def executionData() {

  }

  def testTaskName() {

  }
}
