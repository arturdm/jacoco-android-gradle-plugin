package com.dicedmelon.gradle.jacoco.android

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Task
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.tasks.JacocoReport

class JacocoAndroidPlugin implements Plugin<ProjectInternal> {

  @Override public void apply(ProjectInternal project) {
    project.plugins.apply(JacocoPlugin)

    def plugin = project.plugins.findPlugin('android') ?:
        project.plugins.findPlugin('android-library')
    if (!plugin) {
      throw new GradleException(
          'You must apply the Android plugin or the Android library plugin before using the jacoco-android plugin')
    }

    Task jacocoTestReportTask = project.tasks.findByName("jacocoTestReport")
    if (!jacocoTestReportTask) {
      jacocoTestReportTask = project.tasks.create("jacocoTestReport")
      jacocoTestReportTask.group = "Reporting"
    }

    def isLibraryPlugin = plugin.class.name.endsWith('.LibraryPlugin')

    def variants
    if (isLibraryPlugin) {
      variants = "libraryVariants"
    } else {
      variants = "applicationVariants"
    }

    project.android[variants].all { variant ->
      def sourceDirs = sourceDirs(variant)
      def classesDir = classesDir(variant)
      def testTask = testTask(project.tasks, variant)
      def executionData = executionDataFile(testTask)
      JacocoReport reportTask = project.tasks.create(
          name: "jacoco${testTask.name.capitalize()}Report", type: JacocoReport,
          dependsOn: testTask)
      reportTask.group = "Reporting"
      reportTask.description = "Generates Jacoco coverage reports for the ${variant.name} variant."
      reportTask.executionData executionData
      reportTask.sourceDirectories = project.files(sourceDirs)
      reportTask.classDirectories = project.files(classesDir)
      reportTask.reports {
        xml.enabled true
        html.enabled = true
      }
      jacocoTestReportTask.dependsOn reportTask
    }
  }

  static def sourceDirs(variant) {
    variant.sourceSets.java.srcDirs
  }

  static def classesDir(variant) {
    variant.javaCompile.destinationDir
  }

  static def testTask(TaskCollection<Task> tasks, variant) {
    tasks.withType(Test).find { task -> task.name.contains(variant.name.capitalize()) }
  }

  static def executionDataFile(Task testTask) {
    testTask.jacoco.destinationFile
  }
}
