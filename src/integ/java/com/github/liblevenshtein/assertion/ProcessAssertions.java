package com.github.liblevenshtein.assertion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.NonNull;

import org.apache.commons.lang3.StringEscapeUtils;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

/**
 * AssertJ-style assertions for {@link Process}.
 */
public class ProcessAssertions
    extends AbstractAssert<ProcessAssertions, Process> {

  /**
   * Exit code returned by a process when it completes successfully.
   */
  private static final int EXIT_SUCCESS = 0;

  /**
   * Specifies which lines to exclude (blacklist) from comparison in
   * {@link #toStandardOutput()} and {@link #toStandardError()}.
   */
  private final List<Predicate<String>> exclusions = new LinkedList<>();

  /**
   * Specifies which lines to include (whitelist) from comparison in
   * {@link #toStandardOutput()} and {@link #toStandardError()}.
   */
  private final List<Predicate<String>> inclusions = new LinkedList<>();

  /**
   * Specifies how to rewrite lines in {@link #toStandardOutput()} and
   * {@link #toStandardError()}.
   */
  private final List<Map.Entry<Pattern, String>> replacements = new LinkedList<>();

  /**
   * Hierarchical assertions.  Calls to {@link #include(String)} and
   * {@link #rewrite(String)} call the corresponding methods on {@link #parent}
   * first, and calls to {@link #toStandardOutput()} and
   * {@link #toStandardError()} return {@link #parent} for fluency.
   */
  private final ProcessAssertions parent;

  /**
   * Printed string to assert-against in {@link #toStandardOutput()} and
   * {@link #toStandardError()}.
   */
  private final String output;

  /**
   * Initializes a new {@link ProcessAssertions} with a {@link Process} to
   * assert-against, a parent {@link ProcessAssertions} to reference in various
   * methods, and a printed string to assert-agsinst in
   * {@link #toStandardOutput()} and {@link #toStandardError()}.
   * @param actual {@link Process} to assert-against.
   * @param parent Parent {@link ProcessAssertions} of this one.
   * @param output Printed string to compare in {@link #toStandardOutput()} and
   * {@link #toStandardError()}.
   */
  public ProcessAssertions(
      final Process actual,
      final ProcessAssertions parent,
      final String output) {
    super(actual, ProcessAssertions.class);
    this.parent = parent;
    this.output = output;
  }

  /**
   * Initializes a new {@link ProcessAssertions} with a {@link Process} to
   * assert-against.
   * @param actual {@link Process} to assert-against.
   */
  public ProcessAssertions(final Process actual) {
    this(actual, null, null);
  }

  /**
   * Initializes a new {@link ProcessAssertions} with a {@link Process} to
   * assert-against.
   * @param actual {@link Process} to assert-against.
   * @return New {@link ProcessAssertions} to assert-against.
   */
  public static ProcessAssertions assertThat(final Process actual) {
    return new ProcessAssertions(actual);
  }

  /**
   * Asserts that the process exited with the given code.
   * @param exitCode Expected, exit code of the process.
   * @return This {@link ProcessAssertions} for fluency.
   * @throws AssertionError When the process did not exit with the expected
   * code.
   */
  public ProcessAssertions exitedWith(final int exitCode) {
    isNotNull();
    if (exitCode != actual.exitValue()) {
      failWithMessage("Expected process to exit with [%d], but was [%d]",
        exitCode, actual.exitValue());
    }
    return this;
  }

  /**
   * Asserts that the process did not exit with a specific code.
   * @param exitCode Unexpected, exit code of the process.
   * @return This {@link ProcessAssertions} for fluency.
   * @throws AssertionError When the process exited with the exit code.
   */
  public ProcessAssertions didNotExitWith(final int exitCode) {
    isNotNull();
    if (exitCode == actual.exitValue()) {
      failWithMessage("Did not expect process to exit with [%d]",
        actual.exitValue());
    }
    return this;
  }

  /**
   * Asserts that the process exited successfully.
   * @return This {@link ProcessAssertions} for fluency.
   * @throws AssertionError When the process exited with a failure.
   */
  public ProcessAssertions succeeded() {
    return exitedWith(EXIT_SUCCESS);
  }

  /**
   * Asserts that the process exited with a failure.
   * @return This {@link ProcessAssertions} for fluency.
   * @throws AssertionError When the process exited successfully.
   */
  public ProcessAssertions failed() {
    return didNotExitWith(EXIT_SUCCESS);
  }

  /**
   * Sets the printed value to assert-against in {@link #toStandardOutput()} and
   * {@link #toStandardError()}.  This method does not set {@link #output} on
   * this {@link ProcessAssertions}, but returns a new {@link ProcessAssertions}
   * with {@link #output}.  This is to avoid issues with mutation in the current
   * assertions.
   * @param output Printed value to assert-against.
   * @return New {@link ProcessAssertions} having {@link #output} to
   * assert-against.
   */
  public ProcessAssertions printed(@NonNull final String output) {
    return new ProcessAssertions(actual, this, output);
  }

  /**
   * Adds a rule for lines to ignore when comparing the printed output.
   * @param pattern Specifies lines to exclude.
   * @return This {@link ProcessAssertions} for fluency.
   */
  public ProcessAssertions excluding(@NonNull final String pattern) {
    return excluding(Pattern.compile(pattern));
  }

  /**
   * Adds a rule for lines to ignore when comparing the printed output.
   * @param regex Specifies lines to exclude.
   * @return This {@link ProcessAssertions} for fluency.
   */
  public ProcessAssertions excluding(@NonNull final Pattern regex) {
    return excluding(regex.asPredicate());
  }

  /**
   * Adds a rule for lines to ignore when comparing the printed output.
   * @param exclusion Specifies lines to exclude.
   * @return This {@link ProcessAssertions} for fluency.
   */
  public ProcessAssertions excluding(@NonNull final Predicate<String> exclusion) {
    exclusions.add(exclusion);
    return this;
  }

  /**
   * Adds a rule for lines to include when comparing the printed output.
   * @param pattern Specifies lines to include.
   * @return This {@link ProcessAssertions} for fluency.
   */
  public ProcessAssertions including(@NonNull final String pattern) {
    return including(Pattern.compile(pattern));
  }

  /**
   * Adds a rule for lines to include when comparing the printed output.
   * @param regex Specifies lines to include.
   * @return This {@link ProcessAssertions} for fluency.
   */
  public ProcessAssertions including(@NonNull final Pattern regex) {
    return including(regex.asPredicate());
  }

  /**
   * Adds a rule for lines to include when comparing the printed output.
   * @param inclusion Specifies lines to include.
   * @return This {@link ProcessAssertions} for fluency.
   */
  public ProcessAssertions including(@NonNull final Predicate<String> inclusion) {
    inclusions.add(inclusion);
    return this;
  }

  /**
   * Adds a rule for patterns to remove from lines of the compared output.
   * @param pattern Pattern to remove from lines of the compared output.
   * @return This {@link ProcessAssertions} for fluency.
   */
  public ProcessAssertions stripping(@NonNull final String pattern) {
    return replacing(pattern, "");
  }

  /**
   * Adds a rule for patterns to remove from lines of the compared output.
   * @param regex Pattern to remove from lines of the compared output.
   * @return This {@link ProcessAssertions} for fluency.
   */
  public ProcessAssertions stripping(@NonNull final Pattern regex) {
    return replacing(regex, "");
  }

  /**
   * Removes whitespace from the beginning and ending of lines of the compared
   * output.
   * @return This {@link ProcessAssertions} for fluency.
   */
  public ProcessAssertions trim() {
    return replacing("^\\s+|\\s+$", "");
  }

  /**
   * Specifies a pattern for replacement in lines of the compared output.
   * @param pattern Pattern for replacement.
   * @param replacement What to replace the pattern with.
   * @return This {@link ProcessAssertions} for fluency.
   */
  public ProcessAssertions replacing(
      @NonNull final String pattern,
      @NonNull final String replacement) {
    final Pattern regex = Pattern.compile(pattern);
    return replacing(regex, replacement);
  }

  /**
   * Specifies a pattern for replacement in lines of the compared output.
   * @param regex Pattern for replacement.
   * @param replacement What to replace the pattern with.
   * @return This {@link ProcessAssertions} for fluency.
   */
  public ProcessAssertions replacing(
      @NonNull final Pattern regex,
      @NonNull final String replacement) {
    final Map.Entry<Pattern, String> entry =
      new AbstractMap.SimpleImmutableEntry<>(regex, replacement);
    replacements.add(entry);
    return this;
  }

  /**
   * Determines whether to include a line in the compared output.  This method
   * recursively calls {@link #include(String)} on {@link #paren}, and returns
   * false if the {@link #parent} returns false.
   * @param line Line whose inclusion is to be determined.
   * @return Whether to include {@link #line} in the compared output.
   */
  private boolean include(final String line) {
    if (null != parent && !parent.include(line)) {
      return false;
    }
    for (final Predicate<String> exclusion : exclusions) {
      if (exclusion.test(line)) {
        return false;
      }
    }
    for (final Predicate<String> inclusion : inclusions) {
      if (!inclusion.test(line)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Rewrites a line according to the rules in {@link #replacements} and those
   * of {@link #parent}.
   * @param line Line of output to rewrite.
   * @return Rewritten line.
   */
  private String rewrite(final String line) {
    String curr = line;
    if (null != parent) {
      curr = parent.rewrite(curr);
    }
    for (final Map.Entry<Pattern, String> entry : replacements) {
      final Pattern regex = entry.getKey();
      final Matcher matcher = regex.matcher(curr);
      if (matcher.find()) {
        final String replacement = entry.getValue();
        curr = matcher.replaceAll(replacement);
      }
    }
    return curr;
  }

  /**
   * Asserts that the process printed the transformed {@link #output} to its
   * standard output stream.
   * @return This {@link ProcessAssertions} for fluency.
   * @throws AssertionError Whent the {@link #actual} process did not print the
   * transformed {@link #output} to its standard output stream.
   * @throws IOException If the standard output cannot be read.
   */
  public ProcessAssertions toStandardOutput() throws IOException {
    isNotNull();
    Assertions.assertThat(parent).isNotNull();
    Assertions.assertThat(output).isNotNull();
    final String stdout = standardOutput();
    if (!output.equals(stdout)) {
      failWithMessage("Unexpected process stdout: \"%s\"",
        StringEscapeUtils.escapeJava(stdout));
    }
    return parent;
  }

  /**
   * Asserts that the process printed the transformed {@link #output} to its
   * standard error stream.
   * @return This {@link ProcessAssertions} for fluency.
   * @throws AssertionError Whent the {@link #actual} process did not print the
   * transformed {@link #output} to its standard error stream.
   * @throws IOException If the standard error cannot be read.
   */
  public ProcessAssertions toStandardError() throws IOException {
    isNotNull();
    Assertions.assertThat(parent).isNotNull();
    Assertions.assertThat(output).isNotNull();
    final String stderr = standardError();
    if (!output.equals(stderr)) {
      failWithMessage("Unexpected process stderr: \"%s\"",
        StringEscapeUtils.escapeJava(stderr));
    }
    return parent;
  }

  /**
   * Returns the standard output stream of the {@link #actual} process.
   * @return Standard output stream of the {@link #actual} process.
   * @throws IOException If the standard output cannot be read.
   */
  private String standardOutput() throws IOException {
    return read(actual.getInputStream());
  }

  /**
   * Returns the standard error stream of the {@link #actual} process.
   * @return Standard error stream of the {@link #actual} process.
   * @throws IOException If the standard error cannot be read.
   */
  private String standardError() throws IOException {
    return read(actual.getErrorStream());
  }

  /**
   * Reads a the stream into a string.  This method resolves carriage returns,
   * only includes lines that are not blacklisted, if there is a whitelist it
   * only includes lines that are whitelisted, and it transforms included lines
   * according to the rewrite rules.
   * @param stream Standard output or error stream of the {@link #actual}
   * process.
   * @return Accepted, rewritten lines of the {@link #stream}.
   * @throws IOException When the content cannot be read from {@link #stream}.
   */
  private String read(final InputStream stream) throws IOException {
    final StringBuilder buffer = new StringBuilder();

    try (final BufferedReader reader =
        new BufferedReader(
          new InputStreamReader(stream, StandardCharsets.UTF_8))) {

      final StringBuilder lineBuffer = new StringBuilder();

      for (int c = reader.read(); -1 != c; c = reader.read()) {
        if ('\r' == c) {
          lineBuffer.setLength(0);
        }
        else {
          if ('\n' == c) {
            lineBuffer.append('\n');
            final String line = lineBuffer.toString();
            lineBuffer.setLength(0);

            if (include(line)) {
              buffer.append(rewrite(line));
            }
          }
          else {
            lineBuffer.append((char) c);
          }
        }
      }

      if (0 != lineBuffer.length()) {
        final String line = lineBuffer.toString();
        if (include(line)) {
          buffer.append(rewrite(line));
        }
      }
    }

    return buffer.toString();
  }
}
