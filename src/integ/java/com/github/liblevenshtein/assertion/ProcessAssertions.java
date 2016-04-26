package com.github.liblevenshtein.assertion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

public class ProcessAssertions
    extends AbstractAssert<ProcessAssertions, Process> {

  private final List<Predicate<String>> exclusions = new LinkedList<>();
  private final List<Predicate<String>> inclusions = new LinkedList<>();
  private final List<Map.Entry<Pattern, String>> replacements = new LinkedList<>();

  private final ProcessAssertions parent;
  private final String output;

  public ProcessAssertions(
      final Process actual,
      final ProcessAssertions parent,
      final String output) {
    super(actual, ProcessAssertions.class);
    this.parent = parent;
    this.output = output;
  }

  public ProcessAssertions(final Process actual) {
    this(actual, null, null);
  }

  public static ProcessAssertions assertThat(final Process actual) {
    return new ProcessAssertions(actual);
  }

  public static ProcessAssertions assertThat(final List<String> command) throws IOException {
    final Process actual = new ProcessBuilder(command).start();
    return assertThat(actual);
  }

  public static ProcessAssertions assertThat(final String... command) throws IOException {
    final Process actual = new ProcessBuilder(command).start();
    return assertThat(actual);
  }

  public ProcessAssertions exitedWith(final int exitCode) {
    isNotNull();
    if (exitCode != actual.exitValue()) {
      failWithMessage("Expected process to exit with [%d], but was [%d]",
        exitCode, actual.exitValue());
    }
    return this;
  }

  public ProcessAssertions didNotExitWith(final int exitCode) {
    isNotNull();
    if (exitCode == actual.exitValue()) {
      failWithMessage("Did not expect process to exit with [%d]",
        actual.exitValue());
    }
    return this;
  }

  public ProcessAssertions succeeded() {
    return exitedWith(0);
  }

  public ProcessAssertions failed() {
    return didNotExitWith(0);
  }

  public ProcessAssertions printed(@NonNull final String output) {
    isNotNull();
    return new ProcessAssertions(actual, this, output);
  }

  public ProcessAssertions excluding(@NonNull final String pattern) {
    return excluding(Pattern.compile(pattern));
  }

  public ProcessAssertions excluding(@NonNull final Pattern regex) {
    return excluding(regex.asPredicate());
  }

  public ProcessAssertions excluding(@NonNull final Predicate<String> exclusion) {
    exclusions.add(exclusion);
    return this;
  }

  public ProcessAssertions including(@NonNull final String pattern) {
    return including(Pattern.compile(pattern));
  }

  public ProcessAssertions including(@NonNull final Pattern regex) {
    return including(regex.asPredicate());
  }

  public ProcessAssertions including(@NonNull final Predicate<String> inclusion) {
    inclusions.add(inclusion);
    return this;
  }

  public ProcessAssertions stripping(@NonNull final String pattern) {
    return replacing(pattern, "");
  }

  public ProcessAssertions stripping(@NonNull final Pattern regex) {
    return replacing(regex, "");
  }

  public ProcessAssertions replacing(
      @NonNull final String pattern,
      @NonNull final String replacement) {
    final Pattern regex = Pattern.compile(pattern);
    return replacing(regex, replacement);
  }

  public ProcessAssertions replacing(
      @NonNull final Pattern regex,
      @NonNull final String replacement) {
    final Map.Entry<Pattern, String> entry =
      new AbstractMap.SimpleImmutableEntry<>(regex, replacement);
    replacements.add(entry);
    return this;
  }

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

  private String rewrite(final String line) {
    String curr = line;
    if (null != parent) {
      line = parent.rewrite(line);
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

  private String standardOutput() throws IOException {
    return read(actual.getInputStream());
  }

  private String standardError() throws IOException {
    return read(actual.getErrorStream());
  }

  private String read(final InputStream stream) throws IOException {
    final StringBuilder buffer = new StringBuilder();

    try (final BufferedReader reader =
        new BufferedReader(new InputStreamReader(stream))) {

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
