package com.dicedmelon.gradle.jacoco.android

import org.gradle.api.ProjectConfigurationException
import org.gradle.api.internal.file.collections.DirectoryFileTree
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.tasks.JacocoReport
import spock.lang.Specification
import spock.lang.Unroll

import static com.dicedmelon.gradle.jacoco.android.AndroidProjectFactory.configure
import static com.dicedmelon.gradle.jacoco.android.AndroidProjectFactory.create

class JacocoAndroidPluginSpec extends Specification {

  ProjectInternal project

  def setup() {
    project = create() as ProjectInternal
  }

  def "should throw if android plugin not applied"() {
    when:
    project.apply plugin: JacocoAndroidPlugin
    project.evaluate()

    then:
    thrown ProjectConfigurationException
  }

  def "should throw if unsupported android plugin applied"() {
    when:
    project.apply plugin: JacocoAndroidPlugin
    project.apply plugin: 'com.android.test'
    project.evaluate()

    then:
    thrown ProjectConfigurationException
  }

  def "should apply jacoco plugin by default with com.android.library"() {
    when:
    project.apply plugin: JacocoAndroidPlugin
    project.apply plugin: 'com.android.library'
    configure(project)
    project.evaluate()

    then:
    project.plugins.hasPlugin(JacocoPlugin)
  }

  def "should apply jacoco plugin by default with com.android.application"() {
    when:
    project.apply plugin: 'com.android.application'
    project.apply plugin: JacocoAndroidPlugin
    configure(project)
    project.evaluate()

    then:
    project.plugins.hasPlugin(JacocoPlugin)
  }

  def "should apply jacoco plugin by default with com.android.dynamic-feature"() {
    when:
    project.apply plugin: 'com.android.dynamic-feature'
    project.apply plugin: JacocoAndroidPlugin
    configure(project)
    project.evaluate()

    then:
    project.plugins.hasPlugin(JacocoPlugin)
  }

  def "should add JacocoReport tasks for each variant"() {
    when:
    project.apply plugin: JacocoAndroidPlugin
    project.apply plugin: 'com.android.library'
    configure(project)
    project.evaluate()

    then:
    project.tasks.findByName("jacocoTestPaidDebugUnitTestReport")
    project.tasks.findByName("jacocoTestFreeDebugUnitTestReport")
    project.tasks.findByName("jacocoTestPaidReleaseUnitTestReport")
    project.tasks.findByName("jacocoTestFreeReleaseUnitTestReport")
    project.tasks.findByName("jacocoTestReport")
  }

  def "should use default excludes"() {
    when:
    project.apply plugin: JacocoAndroidPlugin
    project.apply plugin: 'com.android.library'
    configure(project)
    project.evaluate()

    then:
    assertAllJacocoReportTasksExclude(JacocoAndroidUnitTestReportExtension.defaultExcludes)
  }

  def "should use extension's excludes"() {
    when:
    project.apply plugin: JacocoAndroidPlugin
    project.apply plugin: 'com.android.library'
    configure(project)
    project.jacocoAndroidUnitTestReport {
      excludes = ['some exclude']
    }
    project.evaluate()

    then:
    assertAllJacocoReportTasksExclude(['some exclude'])
  }

  def "should merge default and extension's excludes"() {
    when:
    project.apply plugin: JacocoAndroidPlugin
    project.apply plugin: 'com.android.library'
    configure(project)
    project.jacocoAndroidUnitTestReport {
      excludes.add('some exclude')
    }
    project.evaluate()

    then:
    def expected = ['some exclude']
    expected.addAll(JacocoAndroidUnitTestReportExtension.defaultExcludes)
    assertAllJacocoReportTasksExclude(expected)
  }

  @Unroll
  def "should use extension's #report configuration"() {
    when:
    project.apply plugin: JacocoAndroidPlugin
    project.apply plugin: 'com.android.library'
    configure(project)
    project.jacocoAndroidUnitTestReport."$report" = true
    project.evaluate()

    then:
    eachJacocoReportTask {
      assert it.reports."$report".enabled == true
    }

    where:
    report << ['csv', 'html', 'xml']
  }

  def "should apply which reports to build by default"() {
    when:
    project.apply plugin: JacocoAndroidPlugin
    project.apply plugin: 'com.android.library'
    configure(project)
    project.evaluate()

    then:
    eachJacocoReportTask {
      assert it.reports."$report".enabled == enabled
    }

    where:
    report | enabled
    'csv'  | false
    'html' | true
    'xml'  | true
  }

  def "should add kotlin class directories if plugin added"() {
    when:
    project.apply plugin: JacocoAndroidPlugin
    project.apply plugin: 'com.android.library'
    configure(project)
    project.apply plugin: "kotlin-android"
    project.evaluate()

    then:
    eachJacocoReportTask { JacocoReport jacocoReport ->
      Collection<DirectoryFileTree> fileTrees = jacocoReport.classDirectories.asFileTrees
      assert fileTrees.each { it.dir.path.contains("/tmp/kotlin-classes/") }
    }
  }

  def "should use extension's destination"() {
    when:
    project.apply plugin: JacocoAndroidPlugin
    project.apply plugin: 'com.android.library'
    configure(project)
    project.jacocoAndroidUnitTestReport {
      destination = project.file("${project.buildDir}/newJacocoDest")
    }
    project.evaluate()

    then:
    assertAllJacocoReportTasksDestinationRoot("${project.buildDir}/newJacocoDest")
  }

  def "should have a convention destination"() {
    when:
    project.apply plugin: JacocoAndroidPlugin
    project.apply plugin: 'com.android.library'
    configure(project)
    project.evaluate()

    then:
    assertAllJacocoReportTasksDestinationRoot("${project.buildDir}/reports/jacoco/")
  }

  void eachJacocoReportTask(Closure<JacocoReport> closure) {
    project.tasks.withType(JacocoReport).each(closure)
  }

  void assertAllJacocoReportTasksDestinationRoot(String rootPath) {
    eachJacocoReportTask { JacocoReport jacocoReport ->
      assert jacocoReport.reports.csv.destination.path.startsWith(rootPath)
      assert jacocoReport.reports.csv.destination.path.endsWith("jacoco.csv")
      assert jacocoReport.reports.html.destination.path.startsWith(rootPath)
      assert jacocoReport.reports.html.destination.path.endsWith("/html")
      assert jacocoReport.reports.xml.destination.path.startsWith(rootPath)
      assert jacocoReport.reports.xml.destination.path.endsWith("jacoco.xml")
    }
  }

  void assertAllJacocoReportTasksExclude(Collection<String> excludes) {
    eachJacocoReportTask { JacocoReport jacocoReport ->
      assert excludes.containsAll(jacocoReport.classDirectories.from.collect {
        it.get().getExcludes()
      }.flatten())
    }
  }
}
