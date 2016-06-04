package com.github.liblevenshtein.assertion;

import org.testng.annotations.Test;

import static com.github.liblevenshtein.assertion.StringBuilderAssertions.assertThat;

public class StringBuilderAssertionsTest {

  private static final String FOO = "foo";

  private static final String BAR = "bar";

  private static final String BAZ = "baz";

  private static final String QUX = "qux";

  private static final String QUO = "quo";

  @Test
  public void testIsEmptyWhenEmpty() {
    assertThat(new StringBuilder()).isEmpty();
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testIsEmptyWhenNotEmpty() {
    assertThat(new StringBuilder(FOO)).isEmpty();
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testIsEmptyWhenNull() {
    assertThat(null).isEmpty();
  }

  @Test
  public void testIsEqualToWhenEquals() {
    assertThat(new StringBuilder(BAR)).isEqualTo(BAR);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testIsEqualToWhenNotEquals() {
    assertThat(new StringBuilder(BAZ)).isEqualTo(QUX);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testIsEqualToWhenNull() {
    assertThat(null).isEqualTo(QUO);
  }
}
