package com.dicedmelon.gradle.jacoco.android

import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

abstract class JacocoAndroidUnitTestReportExtension {

  public static final Collection<String> androidDataBindingExcludes =
      ['android/databinding/**/*.class',
       '**/android/databinding/*Binding.class',
       '**/BR.*'].asImmutable()

  public static final Collection<String> androidExcludes =
      ['**/R.class',
       '**/R$*.class',
       '**/BuildConfig.*',
       '**/Manifest*.*'].asImmutable()

  public static final Collection<String> butterKnifeExcludes =
      ['**/*$ViewInjector*.*',
       '**/*$ViewBinder*.*'].asImmutable()

  public static final Collection<String> dagger2Excludes =
      ['**/*_MembersInjector.class',
       '**/Dagger*Component.class',
       '**/Dagger*Component$Builder.class',
       '**/*Module_*Factory.class'].asImmutable()

  public static final Collection<String> defaultExcludes =
      (androidDataBindingExcludes + androidExcludes + butterKnifeExcludes + dagger2Excludes)
          .asImmutable()

  abstract ListProperty<String> getExcludes()
  abstract Property<Boolean> getCsv()
  abstract Property<Boolean> getHtml()
  abstract Property<Boolean> getXml()
  abstract DirectoryProperty getDestination()

  JacocoAndroidUnitTestReportExtension(Project project) {
    excludes.convention(defaultExcludes)
    csv.convention(false)
    html.convention(true)
    xml.convention(true)
    destination.convention(project.layout.buildDirectory.dir("reports/jacoco"))
  }
}
