package com.dicedmelon.gradle.jacoco.android

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.logging.Logger
import org.gradle.api.plugins.PluginContainer
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.tasks.JacocoReport

import static org.gradle.api.logging.Logging.getLogger

class JacocoAndroidPlugin implements Plugin<ProjectInternal> {

  Logger logger = getLogger(getClass())

  @Override public void apply(ProjectInternal project) {
    project.extensions.create("jacocoAndroidUnitTestReport",
        JacocoAndroidUnitTestReportExtension,
        JacocoAndroidUnitTestReportExtension.defaultExcludesFactory())
    project.plugins.apply(JacocoPlugin)

    Plugin plugin = findAndroidPluginOrThrow(project.plugins)

    project.afterEvaluate {
      Task jacocoTestReportTask = findOrCreateJacocoTestReportTask(project.tasks)
      def variants = getVariants(project, plugin)
      
      variants.all { variant ->
        JacocoReport reportTask = createReportTask(project, variant)
        jacocoTestReportTask.dependsOn reportTask
        project.tasks{ task -> logger.info(task.name) }
        logTaskAdded(reportTask)
      }
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
    
    boolean integrationTestsEnabled = project.jacocoAndroidUnitTestReport.integrationTestsEnabled
    Task integrationTestTask = getTask(project, getIntegrationTaskName(variant))
    def testTask = testTask(project.tasks, variant)

    def executionData = executionDataFile(testTask, integrationTestsEnabled, project)
    JacocoReport reportTask = project.tasks.create("jacoco${testTask.name.capitalize()}Report",
        JacocoReport)
    if(integrationTestsEnabled){
      integrationTestTask.dependsOn testTask
      reportTask.dependsOn integrationTestTask
    }else {
      reportTask.dependsOn testTask
    }
    reportTask.group = "Reporting"
    reportTask.description = "Generates Jacoco coverage reports for the ${variant.name} variant."
    reportTask.executionData = executionDataFile(testTask, integrationTestsEnabled, project)
    reportTask.sourceDirectories = project.files(sourceDirs)
    reportTask.classDirectories = 
        project.fileTree(dir: classesDir, excludes: project.jacocoAndroidUnitTestReport.excludes)
    reportTask.reports {
      csv.enabled project.jacocoAndroidUnitTestReport.csv.enabled
      html.enabled project.jacocoAndroidUnitTestReport.html.enabled
      xml.enabled project.jacocoAndroidUnitTestReport.xml.enabled
    }
    reportTask
  }

  static def sourceDirs(variant) {
    variant.sourceSets.java.srcDirs.collect { it.path }.flatten()
  }

  static def classesDir(variant) {
    variant.javaCompile.destinationDir
  }

  static def getTask(Project project, String name) {
    project.getTasksByName(name, false)?.getAt(0)
  }

  static String getIntegrationTaskName(def variant) {
      String name = "create${variant.name.capitalize()}CoverageReport"
      name
  }

  static def testTask(TaskCollection<Task> tasks, variant) {
    tasks.withType(Test).find { task -> task.name.contains(variant.name.capitalize()) }
  }

  static def executionDataFile(Task testTask, boolean integrationTestsEnabled, ProjectInternal project) {
    if(integrationTestsEnabled){
      project.fileTree(dir: project.buildDir, includes: ['**/*.exec', '**/*.ec'])
    } else {
      project.files(testTask.jacoco.destinationFile.path)
    }
  }

  private void logTaskAdded(JacocoReport reportTask) {
    logger.info("Added $reportTask")
    logger.info("  executionData: $reportTask.executionData.asPath")
    logger.info("  classDirectories: $reportTask.classDirectories.dir.path")
    logger.info("  sourceDirectories: $reportTask.sourceDirectories.asPath")
  }
}
