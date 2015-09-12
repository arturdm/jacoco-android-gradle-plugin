package com.dicedmelon.gradle.jacoco.android

import org.gradle.api.Project
import org.gradle.api.internal.plugins.PluginApplicationException
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import spock.lang.Specification
import spock.lang.Unroll

class JacocoAndroidPluginSpec extends Specification {

  Project project

  def setup() {
    project = AndroidProjectFactory.create()
  }

  def "should throw if android plugin not applied"() {
    when:
    project.apply plugin: JacocoAndroidPlugin

    then:
    thrown PluginApplicationException
  }

  @Unroll
  def "should apply jacoco plugin by default with #androidPlugin"() {
    expect:
    project.apply plugin: androidPlugin
    project.apply plugin: JacocoAndroidPlugin
    project.plugins.hasPlugin(JacocoPlugin)

    where:
    androidPlugin << ['com.android.library', 'com.android.application']
  }

  def "should add JacocoReport tasks for each variant"() {
    when:
    AndroidProjectFactory.configureAsLibrary(project)
    project.apply plugin: JacocoAndroidPlugin
    project.evaluate()

    then:
    project.tasks.findByName("jacocoTestPaidDebugUnitTestReport")
    project.tasks.findByName("jacocoTestFreeDebugUnitTestReport")
    project.tasks.findByName("jacocoTestPaidReleaseUnitTestReport")
    project.tasks.findByName("jacocoTestFreeReleaseUnitTestReport")
    project.tasks.findByName("jacocoTestReport")
  }
}
