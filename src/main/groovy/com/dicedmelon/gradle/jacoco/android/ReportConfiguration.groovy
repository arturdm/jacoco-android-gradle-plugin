package com.dicedmelon.gradle.jacoco.android

public class ReportConfiguration {

  private boolean enabled

  ReportConfiguration(boolean enabled) {
    this.enabled = enabled
  }

  public boolean isEnabled() {
    enabled
  }

  public void enabled(boolean enabled) {
    this.enabled = enabled
  }
}
