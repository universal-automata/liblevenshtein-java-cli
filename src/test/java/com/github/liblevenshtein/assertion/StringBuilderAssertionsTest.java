package com.github.liblevenshtein.assertion;

import org.testng.annotations.Test;

import static com.github.liblevenshtein.assertion.StringBuilderAssertions.assertThat;

public class StringBuilderAssertionsTest {

  @Test
  public void testIsEmptyWhenEmpty() {
    assertThat(new StringBuilder()).isEmpty();
  }

  @Test(expectedExceptions={AssertionError.class})
  public void testIsEmptyWhenNotEmpty() {
    assertThat(new StringBuilder("foo")).isEmpty();
  }

  @Test(expectedExceptions={AssertionError.class})
  public void testIsEmptyWhenNull() {
    assertThat(null).isEmpty();
  }

  @Test
  public void testIsEqualToWhenEquals() {
    assertThat(new StringBuilder("bar")).isEqualTo("bar");
  }

  @Test(expectedExceptions={AssertionError.class})
  public void testIsEqualToWhenNotEquals() {
    assertThat(new StringBuilder("baz")).isEqualTo("qux");
  }

  @Test(expectedExceptions={AssertionError.class})
  public void testIsEqualToWhenNull() {
    assertThat(null).isEqualTo("quo");
  }
}
