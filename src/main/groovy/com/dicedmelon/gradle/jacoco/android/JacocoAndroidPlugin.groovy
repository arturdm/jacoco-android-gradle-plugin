package com.dicedmelon.gradle.jacoco.android

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Task
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.tasks.JacocoReport
import static org.gradle.api.logging.Logging.getLogger

class JacocoAndroidPlugin implements Plugin<ProjectInternal> {

  Logger logger = getLogger(getClass())

  @Override public void apply(ProjectInternal project) {
    project.extensions.create("jacocoAndroidUnitTestReport", JacocoAndroidUnitTestReportExtension)

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

    def variants = isLibraryPlugin ? "libraryVariants" : "applicationVariants"

    project.android[variants].all { variant ->
      def sourceDirs = sourceDirs(variant)
      def classesDir = classesDir(variant)
      def testTask = testTask(project.tasks, variant)
      def executionData = executionDataFile(testTask)
      JacocoReport reportTask = project.tasks.create("jacoco${testTask.name.capitalize()}Report",
          JacocoReport)
      reportTask.dependsOn testTask
      reportTask.group = "Reporting"
      reportTask.description = "Generates Jacoco coverage reports for the ${variant.name} variant."
      reportTask.executionData = project.files(executionData)
      reportTask.sourceDirectories = project.files(sourceDirs)
      reportTask.classDirectories =
          project.fileTree(dir: classesDir, excludes: project.jacocoAndroidUnitTestReport.excludes)
      reportTask.reports {
        xml.enabled true
      }
      jacocoTestReportTask.dependsOn reportTask

      logger.info("Added $reportTask")
      logger.info("  executionData: $reportTask.executionData.asPath")
      logger.info("  classDirectories: $reportTask.classDirectories.dir.path")
      logger.info("  sourceDirectories: $reportTask.sourceDirectories.asPath")
    }
  }

  static def sourceDirs(variant) {
    variant.sourceSets.java.srcDirs.collect { it.path }.flatten()
  }

  static def classesDir(variant) {
    variant.javaCompile.destinationDir
  }

  static def testTask(TaskCollection<Task> tasks, variant) {
    tasks.withType(Test).find { task -> task.name.contains(variant.name.capitalize()) }
  }

  static def executionDataFile(Task testTask) {
    testTask.jacoco.destinationFile.path
  }
}
