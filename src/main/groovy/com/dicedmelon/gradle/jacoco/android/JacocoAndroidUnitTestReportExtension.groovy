package com.dicedmelon.gradle.jacoco.android

class JacocoAndroidUnitTestReportExtension {

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
      ['**/*$ViewInjector*.*'].asImmutable()

  public static final Collection<String> dagger2Excludes =
      ['**/*_MembersInjector.class',
       '**/Dagger*Component.class',
       '**/Dagger*Component$Builder.class',
       '**/*Module_*Factory.class'].asImmutable()

  public static final Collection<String> defaultExcludes =
      (androidDataBindingExcludes + androidExcludes + butterKnifeExcludes + dagger2Excludes)
          .asImmutable()

  static Closure<Collection<String>> defaultExcludesFactory = { defaultExcludes }

  Collection<String> excludes

  JacocoAndroidUnitTestReportExtension(Collection<String> excludes) {
    this.excludes = excludes
  }
}
