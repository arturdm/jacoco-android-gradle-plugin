package com.dicedmelon.gradle.jacoco.android

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.plugins.InvalidPluginException
import org.gradle.api.plugins.PluginContainer
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

class JacocoAndroidPlugin implements Plugin<Project> {

  @Override void apply(Project target) {
    target.pluginManager.withPlugin("com.android.application") {
      AppExtension appExtension = target.getExtensions().findByName("android") as AppExtension
      configurationAction(target, appExtension.applicationVariants)
    }

    target.pluginManager.withPlugin("com.android.dynamic-feature") {
      AppExtension appExtension = target.getExtensions().findByName("android") as AppExtension
      configurationAction(target, appExtension.applicationVariants)
    }

    target.pluginManager.withPlugin("com.android.library") {
      LibraryExtension libExt = target.getExtensions().findByName("android") as LibraryExtension
      configurationAction(target, libExt.libraryVariants)
    }

    target.afterEvaluate {
      JacocoAndroidUnitTestReportExtension extension = findExtension(target)
      //no extension instance means no configuration action ran
      if (extension == null) {
        boolean androidPresent = target.pluginManager.hasPlugin("com.android.base")
        if (!androidPresent) {
          throw new InvalidPluginException(
              "Failed to apply JacocoAndroidPlugin, no android plugin found on project.")
        } else {
          throw new InvalidPluginException(
              "Failed to apply JacocoAndroidPlugin, unsupported android plugin found. Only application or library are supported.")
        }
      }
    }
  }

  private static void createExtension(Project target) {
    target.extensions.create("jacocoAndroidUnitTestReport",
        JacocoAndroidUnitTestReportExtension,
        target)
  }

  private static JacocoAndroidUnitTestReportExtension findExtension(Project target) {
    return target.extensions.findByType(JacocoAndroidUnitTestReportExtension)
  }

  private static TaskProvider<? extends Task> registerLifecycleTask(TaskContainer tasks) {
    return tasks.register("jacocoTestReport") { lifecycleTask -> lifecycleTask.group = "Reporting" }
  }

  private static void configurationAction(Project target,
      DomainObjectSet<? extends BaseVariant> variants) {
    TaskProvider<Task> lifecycleTaskProvider = registerLifecycleTask(target.tasks)

    createExtension(target)
    target.plugins.apply(JacocoPlugin)

    variants.all { variant ->
      TaskProvider<JacocoReport> variantReportProvider = registerReportTask(target, variant)
      lifecycleTaskProvider.configure { it.dependsOn(variantReportProvider) }
    }
  }

  private static TaskProvider<JacocoReport> registerReportTask(Project project,
      BaseVariant variant) {

    def testTaskProvider = testTask(project.tasks, variant)
    def reportTaskName = "jacoco${testTaskProvider.name.capitalize()}Report"
    return project.tasks.register(reportTaskName, JacocoReport) { reportTask ->
      JacocoAndroidUnitTestReportExtension extension = findExtension(project)
      reportTask.group = "Reporting"

      reportTask.description = "Generates Jacoco coverage reports for the ${variant.name} variant."

      reportTask.executionData.from(executionDataFile(testTaskProvider))

      reportTask.sourceDirectories.from(sourceDirs(variant))

      reportTask.classDirectories.from(extension.excludes.map {
        project.fileTree(dir: classesDir(variant), excludes: it)
      })

      if (hasKotlin(project.plugins)) {
        reportTask.classDirectories.from(extension.excludes.map {
          project.fileTree(dir: "${project.buildDir}/tmp/kotlin-classes/${variant.name}",
              excludes: it)
        })
      }

      reportTask.reports { reportContainer ->
        Provider<Directory> reportRoot = extension.destination
            .map { it.dir("test") }
            .map { it.dir(variant.dirName) }

        reportContainer.csv.setEnabled(extension.csv)
        reportContainer.csv.setDestination(reportRoot
            .map { it.file("jacoco.csv") }
            .map { it.asFile })

        reportContainer.html.setEnabled(extension.html)
        reportContainer.html.setDestination(reportRoot
            .map { it.dir("html") }
            .map { it.asFile })

        reportContainer.xml.setEnabled(extension.xml)
        reportContainer.xml.setDestination(reportRoot
            .map { it.file("jacoco.xml") }
            .map { it.asFile })
      }
    } as TaskProvider<JacocoReport>
  }

  static List<File> sourceDirs(BaseVariant variant) {
    return variant.sourceSets.collect { it.javaDirectories }.flatten() as List<File>
  }

  static Provider<File> classesDir(BaseVariant variant) {
    return variant.javaCompileProvider.map { javaCompile -> javaCompile.destinationDir }
  }

  static TaskProvider<Test> testTask(TaskCollection<Task> tasks, variant) {
    return tasks.named("test${variant.name.capitalize()}UnitTest", Test)
  }

  static Provider<File> executionDataFile(TaskProvider<Test> provider) {
    return provider.map { testTask ->
      testTask.extensions
          .getByType(JacocoTaskExtension)
          .destinationFile
    }
  }

  private static boolean hasKotlin(PluginContainer plugins) {
    return plugins.hasPlugin('kotlin-android')
  }
}
