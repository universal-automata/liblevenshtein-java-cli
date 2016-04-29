package com.github.liblevenshtein.assertion;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static com.github.liblevenshtein.assertion.ProcessAssertions.assertThat;

public class ProcessAssertionsTest {

  private static final int EXIT_SUCCESS = 0;

  private static final int EXIT_FAILURE = 1;

  private static final String STDOUT =
    "foo bar\n"
  + "baz qux\n"
  + "foo quo\n"
  + "IGNORE foo\n";

  private static final String STDERR =
    "ERROR: Did not expect baz.\n"
  + "ERROR: Did not expect quo.\n"
  + "IGNORE foo\n";

  private final ThreadLocal<Process> proc = new ThreadLocal<>();

  @BeforeMethod
  public void setUp() {
    proc.set(mock(Process.class));
    when(proc.get().getInputStream())
      .thenReturn(
        new ByteArrayInputStream(
          STDOUT.getBytes(StandardCharsets.UTF_8)));
    when(proc.get().getErrorStream())
      .thenReturn(
        new ByteArrayInputStream(
          STDERR.getBytes(StandardCharsets.UTF_8)));
    when(proc.get().exitValue()).thenReturn(EXIT_SUCCESS);
  }

  @Test
  public void testExitSuccess() {
    assertThat(proc.get())
      .exitedWith(EXIT_SUCCESS)
      .didNotExitWith(EXIT_FAILURE)
      .succeeded();
  }

  @Test
  public void testExitFailure() {
    when(proc.get().exitValue()).thenReturn(EXIT_FAILURE);
    assertThat(proc.get())
      .exitedWith(EXIT_FAILURE)
      .didNotExitWith(EXIT_SUCCESS)
      .failed();
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testExitedWithSuccessAgainstFailure() {
    when(proc.get().exitValue()).thenReturn(EXIT_FAILURE);
    assertThat(proc.get()).exitedWith(EXIT_SUCCESS);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testSucceededAgainstFailure() {
    when(proc.get().exitValue()).thenReturn(EXIT_FAILURE);
    assertThat(proc.get()).succeeded();
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testDidNotExitWithFailureAgainstFailure() {
    when(proc.get().exitValue()).thenReturn(EXIT_FAILURE);
    assertThat(proc.get()).didNotExitWith(EXIT_FAILURE);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testExitedWithFailureAgainstSuccess() {
    assertThat(proc.get()).exitedWith(EXIT_FAILURE);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testFailedAgainstSuccess() {
    assertThat(proc.get()).failed();
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testDidNotExitWithSuccessAgainstSuccess() {
    assertThat(proc.get()).didNotExitWith(EXIT_SUCCESS);
  }

  @Test
  public void testIncludingExcludingReplacingStrippingAndTrim() throws IOException {
    assertThat(proc.get())
      .excluding("IGNORE")
      .replacing("quo", "foo")
      .printed("quux")
        .including("foo")
        .excluding("quo")
        .replacing("bar", "quux")
        .stripping("foo")
        .trim()
        .toStandardOutput()
      .printed("quux")
        .excluding("baz")
        .replacing("^.* (foo)", "$1")
        .replacing("foo", "quux")
        .stripping("\\.")
        .trim()
        .toStandardError();
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testStdoutAgainstFailure() throws IOException {
    assertThat(proc.get())
      .printed("quux")
        .toStandardOutput();
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testStderrAgainstFailure() throws IOException {
    assertThat(proc.get())
      .printed("quux")
        .toStandardError();
  }
}
