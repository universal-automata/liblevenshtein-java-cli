package com.github.liblevenshtein.assertion;

import org.apache.commons.lang3.StringEscapeUtils;

import org.assertj.core.api.AbstractAssert;

import lombok.NonNull;

/**
 * AssertJ-style assertions for {@link StringBuilder}.
 */
public class StringBuilderAssertions
    extends AbstractAssert<StringBuilderAssertions, StringBuilder> {

  /**
   * Constructs a set of assertions around some {@link StringBuilder}.
   * @param actual {@link StringBuilder} to assert-against.
   */
  public StringBuilderAssertions(final StringBuilder actual) {
    super(actual, StringBuilderAssertions.class);
  }

  /**
   * Constructs a set of assertions around some {@link StringBuilder}.
   * @param actual {@link StringBuilder} to assert-against.
   * @return New {@link StringBuilderAssertions} to assert-against.
   */
  public static StringBuilderAssertions assertThat(final StringBuilder actual) {
    return new StringBuilderAssertions(actual);
  }

  /**
   * Asserts that the {@link #actual} {@link StringBuilder} is empty.
   * @return This {@link StringBuilderAssertions} for fluency.
   * @throws AssertionError When the {@link #actual} {@link StringBuilder} is
   *   null or is not empty.
   */
  public StringBuilderAssertions isEmpty() {
    isNotNull();
    if (0 != actual.length()) {
      failWithMessage("Expected StringBuilder to be empty, "
          + "but has length [%d] and contains \"%s\"",
        actual.length(),
        StringEscapeUtils.escapeJava(actual.toString()));
    }
    return this;
  }

  /**
   * Asserts that the {@link #actual} {@link StringBuilder} contains the
   * specific text.
   * @param text Text to cmopare against the {@link #actual}
   * {@link StringBuilder}'s value.
   * @return This {@link StringBuilderAssertions} for fluency.
   * @throws AssertionError When the {@link #actual} {@link StringBuilder} is
   *   null or does not contain the specific text.
   */
  public StringBuilderAssertions isEqualTo(@NonNull final String text) {
    isNotNull();
    if (!text.equals(actual.toString())) {
      failWithMessage("Expected StringBuilder to have \"%s\", but had \"%s\"",
        StringEscapeUtils.escapeJava(text),
        StringEscapeUtils.escapeJava(actual.toString()));
    }
    return this;
  }
}
