package com.dicedmelon.gradle.jacoco.android

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Task
import org.gradle.api.file.FileTree
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.logging.Logger
import org.gradle.api.plugins.PluginContainer
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.TaskContainer
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.tasks.JacocoReport

import static org.gradle.api.logging.Logging.getLogger

class JacocoAndroidPlugin implements Plugin<ProjectInternal> {

  Logger logger = getLogger(getClass())

  @Override void apply(ProjectInternal project) {
    project.extensions.create("jacocoAndroidUnitTestReport",
        JacocoAndroidUnitTestReportExtension,
        JacocoAndroidUnitTestReportExtension.defaultExcludesFactory())
    project.plugins.apply(JacocoPlugin)

    Plugin plugin = findAndroidPluginOrThrow(project.plugins)
    Task jacocoTestReportTask = findOrCreateJacocoTestReportTask(project.tasks)
    def variants = getVariants(project, plugin)

    variants.all { variant ->
      JacocoReport reportTask = createReportTask(project, variant)
      jacocoTestReportTask.dependsOn reportTask

      logTaskAdded(reportTask)
    }
  }

  private static Plugin findAndroidPluginOrThrow(PluginContainer plugins) {
    Plugin plugin = plugins.findPlugin('android') ?: plugins.findPlugin('android-library')
    if (!plugin) {
      throw new GradleException(
          'You must apply the Android plugin or the Android library plugin before using the jacoco-android plugin')
    }
    plugin
  }

  private static Task findOrCreateJacocoTestReportTask(TaskContainer tasks) {
    Task jacocoTestReportTask = tasks.findByName("jacocoTestReport")
    if (!jacocoTestReportTask) {
      jacocoTestReportTask = tasks.create("jacocoTestReport")
      jacocoTestReportTask.group = "Reporting"
    }
    jacocoTestReportTask
  }

  private static def getVariants(ProjectInternal project, Plugin plugin) {
    boolean isLibraryPlugin = plugin.class.name.endsWith('.LibraryPlugin')
    project.android[isLibraryPlugin ? "libraryVariants" : "applicationVariants"]
  }

  private static JacocoReport createReportTask(ProjectInternal project, variant) {
    def sourceDirs = sourceDirs(variant)
    def classesDir = classesDir(variant)
    def testTask = testTask(project.tasks, variant)
    def executionData = executionDataFile(testTask)
    def kotlin = hasKotlin(project.plugins)
    JacocoReport reportTask = project.tasks.create("jacoco${testTask.name.capitalize()}Report",
        JacocoReport)
    reportTask.dependsOn testTask
    reportTask.group = "Reporting"
    reportTask.description = "Generates Jacoco coverage reports for the ${variant.name} variant."
    reportTask.executionData.setFrom(project.files(executionData))
    reportTask.sourceDirectories.setFrom(project.files(sourceDirs))
    FileTree javaTree = project.fileTree(dir: classesDir, excludes: project.jacocoAndroidUnitTestReport.excludes)

    if (kotlin) {
      def kotlinClassesDir = "${project.buildDir}/tmp/kotlin-classes/${variant.name}"
      def kotlinTree =
          project.fileTree(dir: kotlinClassesDir, excludes: project.jacocoAndroidUnitTestReport.excludes)
      reportTask.classDirectories.setFrom(javaTree + kotlinTree)
    } else {
      reportTask.classDirectories.setFrom(javaTree)
    }
    reportTask.reports {
      def destination = project.jacocoAndroidUnitTestReport.destination
      
      csv.enabled project.jacocoAndroidUnitTestReport.csv.enabled
      html.enabled project.jacocoAndroidUnitTestReport.html.enabled
      xml.enabled project.jacocoAndroidUnitTestReport.xml.enabled

      if (csv.enabled) {
        csv.destination new File((destination == null) ? "${project.buildDir}/jacoco/jacoco.csv" : "${destination.trim()}/jacoco.csv")
      }
      
      if (html.enabled) {
        html.destination new File((destination == null) ? "${project.buildDir}/jacoco/jacocoHtml" : "${destination.trim()}/jacocoHtml")
      }

      if (xml.enabled) {
        xml.destination new File((destination == null) ? "${project.buildDir}/jacoco/jacoco.xml" : "${destination.trim()}/jacoco.xml")
      }
    }
    reportTask
  }

  static def sourceDirs(variant) {
    variant.sourceSets.java.srcDirs.collect { it.path }.flatten()
  }

  static def classesDir(variant) {
    if (variant.hasProperty('javaCompileProvider')) {
      variant.javaCompileProvider.get().destinationDir
    } else {
      variant.javaCompile.destinationDir
    }
  }

  static def testTask(TaskCollection<Task> tasks, variant) {
    tasks.getByName("test${variant.name.capitalize()}UnitTest")
  }

  static def executionDataFile(Task testTask) {
    testTask.jacoco.destinationFile.path
  }

  private static boolean hasKotlin(PluginContainer plugins) {
    plugins.findPlugin('kotlin-android')
  }

  private void logTaskAdded(JacocoReport reportTask) {
    logger.info("Added $reportTask")
    logger.info("  executionData: $reportTask.executionData.asPath")
    logger.info("  sourceDirectories: $reportTask.sourceDirectories.asPath")
    logger.info("  csv.destination: $reportTask.reports.csv.destination")
    logger.info("  xml.destination: $reportTask.reports.xml.destination")
    logger.info("  html.destination: $reportTask.reports.html.destination")

  }
}
