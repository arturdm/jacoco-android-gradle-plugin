package com.dicedmelon.example.android;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NumberProviderTest {

  @Test public void shouldProvideProperNumber() {
    // given
    NumberProvider numberProvider = new NumberProvider();

    // when
    int number = numberProvider.provideNumber();

    // then
    assertThat(number).isEqualTo(42);
  }
}
