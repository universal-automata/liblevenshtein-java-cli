package com.github.liblevenshtein.util;

import lombok.experimental.ExtensionMethod;

import org.testng.annotations.Test;

import static com.github.liblevenshtein.assertion.StringBuilderAssertions.assertThat;

@ExtensionMethod({HighlightUtils.class})
public class HighlightUtilsTest {

  @Test
  public void test() {
    final StringBuilder buffer = new StringBuilder();
    assertThat(buffer).isEmpty();
    assertThat(buffer.mode(HighlightUtils.BOLD)).isEqualTo("\u001B[1m");
    assertThat(buffer.background(HighlightUtils.RED))
      .isEqualTo("\u001B[1m\u001B[41m");
    assertThat(buffer.foreground(HighlightUtils.GREEN))
      .isEqualTo("\u001B[1m\u001B[41m\u001B[32m");
    assertThat(buffer.append("foo"))
      .isEqualTo("\u001B[1m\u001B[41m\u001B[32mfoo");
    assertThat(buffer.end())
      .isEqualTo("\u001B[1m\u001B[41m\u001B[32mfoo\u001B[0m");
  }
}
