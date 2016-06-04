package com.github.liblevenshtein;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import lombok.extern.slf4j.Slf4j;

import static com.github.liblevenshtein.assertion.ProcessAssertions.assertThat;

@Slf4j
@SuppressWarnings("checkstyle:multiplestringliterals")
public class CommandLineInterfaceIntegTest {

  private static final Joiner SPACES = Joiner.on(" ");

  private static final Joiner NEWLINES = Joiner.on("\n");

  private static final String QUERY_TERM_1 = "fro";

  private static final String QUERY_TERM_2 = "eb";

  private static final String STANDARD_OUTPUT_WITH_DISTANCES = NEWLINES.join(
    "+-------------------------------------------------------------------------------",
    "| Spelling Candidates for Query Term: \"fro\"",
    "+-------------------------------------------------------------------------------",
    "| d(\"fro\", \"do\") = [2]",
    "| d(\"fro\", \"to\") = [2]",
    "| d(\"fro\", \"for\") = [2]",
    "+-------------------------------------------------------------------------------",
    "| Spelling Candidates for Query Term: \"eb\"",
    "+-------------------------------------------------------------------------------",
    "| d(\"eb\", \"I\") = [2]",
    "| d(\"eb\", \"a\") = [2]",
    "| d(\"eb\", \"as\") = [2]",
    "| d(\"eb\", \"at\") = [2]",
    "| d(\"eb\", \"be\") = [2]",
    "| d(\"eb\", \"do\") = [2]",
    "| d(\"eb\", \"he\") = [2]",
    "| d(\"eb\", \"in\") = [2]",
    "| d(\"eb\", \"it\") = [2]",
    "| d(\"eb\", \"of\") = [2]",
    "| d(\"eb\", \"on\") = [2]",
    "| d(\"eb\", \"to\") = [2]",
    "");

  private static final String STANDARD_OUTPUT_WITHOUT_DISTANCES = NEWLINES.join(
    "+-------------------------------------------------------------------------------",
    "| Spelling Candidates for Query Term: \"fro\"",
    "+-------------------------------------------------------------------------------",
    "| \"fro\" ~ \"do\"",
    "| \"fro\" ~ \"to\"",
    "| \"fro\" ~ \"for\"",
    "+-------------------------------------------------------------------------------",
    "| Spelling Candidates for Query Term: \"eb\"",
    "+-------------------------------------------------------------------------------",
    "| \"eb\" ~ \"I\"",
    "| \"eb\" ~ \"a\"",
    "| \"eb\" ~ \"as\"",
    "| \"eb\" ~ \"at\"",
    "| \"eb\" ~ \"be\"",
    "| \"eb\" ~ \"do\"",
    "| \"eb\" ~ \"he\"",
    "| \"eb\" ~ \"in\"",
    "| \"eb\" ~ \"it\"",
    "| \"eb\" ~ \"of\"",
    "| \"eb\" ~ \"on\"",
    "| \"eb\" ~ \"to\"",
    "");

  private static final String TRANSPOSITION_OUTPUT_WITH_DISTANCES = NEWLINES.join(
    "+-------------------------------------------------------------------------------",
    "| Spelling Candidates for Query Term: \"fro\"",
    "+-------------------------------------------------------------------------------",
    "| d(\"fro\", \"do\") = [2]",
    "| d(\"fro\", \"to\") = [2]",
    "| d(\"fro\", \"for\") = [1]",
    "+-------------------------------------------------------------------------------",
    "| Spelling Candidates for Query Term: \"eb\"",
    "+-------------------------------------------------------------------------------",
    "| d(\"eb\", \"I\") = [2]",
    "| d(\"eb\", \"a\") = [2]",
    "| d(\"eb\", \"as\") = [2]",
    "| d(\"eb\", \"at\") = [2]",
    "| d(\"eb\", \"be\") = [1]",
    "| d(\"eb\", \"do\") = [2]",
    "| d(\"eb\", \"he\") = [2]",
    "| d(\"eb\", \"in\") = [2]",
    "| d(\"eb\", \"it\") = [2]",
    "| d(\"eb\", \"of\") = [2]",
    "| d(\"eb\", \"on\") = [2]",
    "| d(\"eb\", \"to\") = [2]",
    "");

  private static final String TRANSPOSITION_OUTPUT_WITHOUT_DISTANCES = NEWLINES.join(
    "+-------------------------------------------------------------------------------",
    "| Spelling Candidates for Query Term: \"fro\"",
    "+-------------------------------------------------------------------------------",
    "| \"fro\" ~ \"do\"",
    "| \"fro\" ~ \"to\"",
    "| \"fro\" ~ \"for\"",
    "+-------------------------------------------------------------------------------",
    "| Spelling Candidates for Query Term: \"eb\"",
    "+-------------------------------------------------------------------------------",
    "| \"eb\" ~ \"I\"",
    "| \"eb\" ~ \"a\"",
    "| \"eb\" ~ \"as\"",
    "| \"eb\" ~ \"at\"",
    "| \"eb\" ~ \"be\"",
    "| \"eb\" ~ \"do\"",
    "| \"eb\" ~ \"he\"",
    "| \"eb\" ~ \"in\"",
    "| \"eb\" ~ \"it\"",
    "| \"eb\" ~ \"of\"",
    "| \"eb\" ~ \"on\"",
    "| \"eb\" ~ \"to\"",
    "");

  private static final String MERGE_AND_SPLIT_OUTPUT_WITH_DISTANCES = NEWLINES.join(
    "+-------------------------------------------------------------------------------",
    "| Spelling Candidates for Query Term: \"fro\"",
    "+-------------------------------------------------------------------------------",
    "| d(\"fro\", \"I\") = [2]",
    "| d(\"fro\", \"a\") = [2]",
    "| d(\"fro\", \"as\") = [2]",
    "| d(\"fro\", \"at\") = [2]",
    "| d(\"fro\", \"be\") = [2]",
    "| d(\"fro\", \"do\") = [1]",
    "| d(\"fro\", \"he\") = [2]",
    "| d(\"fro\", \"in\") = [2]",
    "| d(\"fro\", \"it\") = [2]",
    "| d(\"fro\", \"of\") = [2]",
    "| d(\"fro\", \"on\") = [2]",
    "| d(\"fro\", \"to\") = [1]",
    "| d(\"fro\", \"and\") = [2]",
    "| d(\"fro\", \"for\") = [2]",
    "| d(\"fro\", \"not\") = [2]",
    "| d(\"fro\", \"the\") = [2]",
    "| d(\"fro\", \"you\") = [2]",
    "+-------------------------------------------------------------------------------",
    "| Spelling Candidates for Query Term: \"eb\"",
    "+-------------------------------------------------------------------------------",
    "| d(\"eb\", \"I\") = [1]",
    "| d(\"eb\", \"a\") = [1]",
    "| d(\"eb\", \"as\") = [2]",
    "| d(\"eb\", \"at\") = [2]",
    "| d(\"eb\", \"be\") = [2]",
    "| d(\"eb\", \"do\") = [2]",
    "| d(\"eb\", \"he\") = [2]",
    "| d(\"eb\", \"in\") = [2]",
    "| d(\"eb\", \"it\") = [2]",
    "| d(\"eb\", \"of\") = [2]",
    "| d(\"eb\", \"on\") = [2]",
    "| d(\"eb\", \"to\") = [2]",
    "| d(\"eb\", \"and\") = [2]",
    "| d(\"eb\", \"for\") = [2]",
    "| d(\"eb\", \"not\") = [2]",
    "| d(\"eb\", \"the\") = [2]",
    "| d(\"eb\", \"you\") = [2]",
    "| d(\"eb\", \"have\") = [2]",
    "| d(\"eb\", \"that\") = [2]",
    "| d(\"eb\", \"with\") = [2]",
    "");

  private static final String MERGE_AND_SPLIT_OUTPUT_WITHOUT_DISTANCES = NEWLINES.join(
    "+-------------------------------------------------------------------------------",
    "| Spelling Candidates for Query Term: \"fro\"",
    "+-------------------------------------------------------------------------------",
    "| \"fro\" ~ \"I\"",
    "| \"fro\" ~ \"a\"",
    "| \"fro\" ~ \"as\"",
    "| \"fro\" ~ \"at\"",
    "| \"fro\" ~ \"be\"",
    "| \"fro\" ~ \"do\"",
    "| \"fro\" ~ \"he\"",
    "| \"fro\" ~ \"in\"",
    "| \"fro\" ~ \"it\"",
    "| \"fro\" ~ \"of\"",
    "| \"fro\" ~ \"on\"",
    "| \"fro\" ~ \"to\"",
    "| \"fro\" ~ \"and\"",
    "| \"fro\" ~ \"for\"",
    "| \"fro\" ~ \"not\"",
    "| \"fro\" ~ \"the\"",
    "| \"fro\" ~ \"you\"",
    "+-------------------------------------------------------------------------------",
    "| Spelling Candidates for Query Term: \"eb\"",
    "+-------------------------------------------------------------------------------",
    "| \"eb\" ~ \"I\"",
    "| \"eb\" ~ \"a\"",
    "| \"eb\" ~ \"as\"",
    "| \"eb\" ~ \"at\"",
    "| \"eb\" ~ \"be\"",
    "| \"eb\" ~ \"do\"",
    "| \"eb\" ~ \"he\"",
    "| \"eb\" ~ \"in\"",
    "| \"eb\" ~ \"it\"",
    "| \"eb\" ~ \"of\"",
    "| \"eb\" ~ \"on\"",
    "| \"eb\" ~ \"to\"",
    "| \"eb\" ~ \"and\"",
    "| \"eb\" ~ \"for\"",
    "| \"eb\" ~ \"not\"",
    "| \"eb\" ~ \"the\"",
    "| \"eb\" ~ \"you\"",
    "| \"eb\" ~ \"have\"",
    "| \"eb\" ~ \"that\"",
    "| \"eb\" ~ \"with\"",
    "");

  private static final String HELP_TEXT = NEWLINES.join(
    "usage: liblevenshtein-java-cli [-a <ALGORITHM>] [--colorize] [-d",
    "       <PATH|URI>] [-h] [-i] [-m <INTEGER>] [-q <STRING> <...>] [-s]",
    "       [--serialize <PATH>] [--source-format <FORMAT>] [--target-format",
    "       <FORMAT>]",
    "",
    "Command-Line Interface to liblevenshtein (Java)",
    "",
    "<FORMAT> specifies the serialization format of the dictionary,",
    "and may be one of the following:",
    "  1. PROTOBUF",
    "     - (de)serialize the dictionary as a protobuf stream.",
    "     - This is the preferred format.",
    "     - See: https://developers.google.com/protocol-buffers/",
    "  2. BYTECODE",
    "     - (de)serialize the dictionary as a Java, bytecode stream.",
    "  3. PLAIN_TEXT",
    "     - (de)serialize the dictionary as a plain text file.",
    "     - Terms are delimited by newlines.",
    "",
    "<ALGORITHM> specifies the Levenshtein algorithm to use for",
    "querying-against the dictionary, and may be one of the following:",
    "  1. STANDARD",
    "     - Use the standard, Levenshtein distance which considers the",
    "     following elementary operations:",
    "       o Insertion",
    "       o Deletion",
    "       o Substitution",
    "     - An elementary operation is an operation that incurs a penalty of",
    "     one unit.",
    "  2. TRANSPOSITION",
    "     - Extend the standard, Levenshtein distance to include transpositions",
    "     as elementary operations.",
    "       o A transposition is a swapping of two, consecutive characters as",
    "       follows: ba -> ab",
    "       o With the standard distance, this would require at least two",
    "       operations:",
    "         + An insertion and a deletion",
    "         + A deletion and an insertion",
    "         + Two substitutions",
    "  3. MERGE_AND_SPLIT",
    "     - Extend the standard, Levenshtein distance to include merges and",
    "     splits as elementary operations.",
    "       o A merge takes two characters and merges them into a single one.",
    "         + For example: ab -> c",
    "       o A split takes a single character and splits it into two others",
    "         + For example: a -> bc",
    "       o With the standard distance, these would require at least two",
    "       operations:",
    "         + Merge:",
    "           > A deletion and a substitution",
    "           > A substitution and a deletion",
    "         + Split:",
    "           > An insertion and a substitution",
    "           > A substitution and an insertion",
    "",
    " -a,--algorithm <ALGORITHM>    Levenshtein algorithm to use (Default:",
    "                               TRANSPOSITION)",
    "    --colorize                 Colorize output",
    " -d,--dictionary <PATH|URI>    Filesystem path or Java-compatible URI to a",
    "                               dictionary of terms",
    " -h,--help                     print this help text",
    " -i,--include-distance         Include the Levenshtein distance with each",
    "                               spelling candidate (Default: false)",
    " -m,--max-distance <INTEGER>   Maximun, Levenshtein distance a spelling",
    "                               candidatemay be from the query term",
    "                               (Default: 2)",
    " -q,--query <STRING> <...>     Terms to query against the dictionary.  You",
    "                               may specify multiple terms.",
    " -s,--is-sorted                Specifies that the dictionary is sorted",
    "                               lexicographically, in ascending order",
    "                               (Default: false)",
    "    --serialize <PATH>         Path to save the serialized dictionary",
    "    --source-format <FORMAT>   Format of the source dictionary (Default:",
    "                               adaptively-try each format until one works)",
    "    --target-format <FORMAT>   Format of the serialized dictionary",
    "                               (Default: PROTOBUF)",
    "",
    "Example: liblevenshtein-java-cli \\",
    "  --algorithm TRANSPOSITION \\",
    "  --max-distance 2 \\",
    "  --include-distance \\",
    "  --query mispelled mispelling \\",
    "  --colorize",
    "");

  /** Strips syntax coloring from output. */
  private static final Pattern RE_COLOR =
    Pattern.compile("\\e\\[(?:\\d+(?:;\\d+)*)?m");

  @Test
  public void testHelp() throws IOException, InterruptedException {
    assertThat(exec(false, false, false))
      .failed()
      .printed(HELP_TEXT)
        .toStandardOutput();
    assertThat(exec(false, false, false, "-h"))
      .succeeded()
      .printed(HELP_TEXT)
        .toStandardOutput();
    assertThat(exec(false, false, false, "--help"))
      .succeeded()
      .printed(HELP_TEXT)
        .toStandardOutput();
    assertThat(exec(false, false, false, "--error"))
      .failed()
      .printed(HELP_TEXT)
        .toStandardOutput();
    assertThat(exec(false, false, false, "--query", "foo", "-h"))
      .succeeded()
      .printed(HELP_TEXT)
        .toStandardOutput();
    assertThat(exec(false, false, false, "--query", "foo", "--help"))
      .succeeded()
      .printed(HELP_TEXT)
        .toStandardOutput();
  }

  @SuppressWarnings({"unchecked", "checkstyle:illegalcatch"})
  @DataProvider(name = "sourceToTargetProvider")
  public Object[][] sourceToTargetProvider() {
    try {
      final boolean[] booleans = {false, true};
      final boolean[] includeDistances = booleans;
      final boolean[] colorizes = booleans;
      final boolean[] isSorteds = booleans;
      final String[] algorithms = {"STANDARD", "TRANSPOSITION", "MERGE_AND_SPLIT"};
      final String[] formats = {"PLAIN_TEXT", "PROTOBUF", "BYTECODE"};

      final List<Object[]> provider =
        new ArrayList<>(
          includeDistances.length
          * colorizes.length
          * isSorteds.length
          * algorithms.length
          * formats.length
          * formats.length);

      for (final String algorithm : algorithms) {
        for (final boolean includeDistance : includeDistances) {
          final String output;

          switch (algorithm) {
            case "STANDARD":
              output = includeDistance
                ? STANDARD_OUTPUT_WITH_DISTANCES
                : STANDARD_OUTPUT_WITHOUT_DISTANCES;
              break;
            case "TRANSPOSITION":
              output = includeDistance
                ? TRANSPOSITION_OUTPUT_WITH_DISTANCES
                : TRANSPOSITION_OUTPUT_WITHOUT_DISTANCES;
              break;
            case "MERGE_AND_SPLIT":
              output = includeDistance
                ? MERGE_AND_SPLIT_OUTPUT_WITH_DISTANCES
                : MERGE_AND_SPLIT_OUTPUT_WITHOUT_DISTANCES;
              break;
            default:
              final String message =
                String.format("Unrecognized algorithm [%s]", algorithm);
              throw new IllegalArgumentException(message);
          }

          for (final boolean isSorted : isSorteds) {
            for (final boolean colorize : colorizes) {
              for (final String sourceFormat : formats) {
                for (final String targetFormat : formats) {
                  provider.add(new Object[] {
                    isSorted,
                    algorithm,
                    includeDistance,
                    output,
                    colorize,
                    sourceFormat,
                    targetFormat,
                  });
                }
              }
            }
          }
        }
      }

      return provider.toArray(new Object[0][0]);
    }
    catch (final Throwable thrown) {
      log.error("Failed to generate sourceToTargetProvider", thrown);
      throw thrown;
    }
  }

  @Test(dataProvider = "sourceToTargetProvider")
  public void testWithDistanceAndDictionaryFormatAndConversionFormat(
      final boolean isSorted,
      final String algorithm,
      final boolean includeDistance,
      final String output,
      final boolean colorize,
      final String dictionaryFormat,
      final String conversionFormat)
      throws IOException, InterruptedException {

    final Path dictionaryPath = tmp("dictionary-", "." + dictionaryFormat);
    final Path conversionPath = tmp("dictionary-", "." + conversionFormat);

    try {

      // Pull the dictionary from the Jar
      assertThat(exec(
            isSorted,
            includeDistance,
            colorize,
            "--dictionary",
              String.format("%s/build/resources/integ/top-20-most-common-english-words.protobuf.bytes",
                System.getProperty("user.dir")),
            "--source-format", "PROTOBUF",
            "--serialize", dictionaryPath,
            "--target-format", dictionaryFormat))
        .succeeded();

      // Convert the dictionary to some other format
      assertThat(
        exec(
          isSorted,
          includeDistance,
          colorize,
          "--dictionary", dictionaryPath,
          "--source-format", dictionaryFormat,
          "--serialize", conversionPath,
          "--target-format", conversionFormat))
        .succeeded();

      // Test formatted parsing

      assertThat(
        exec(
          isSorted,
          includeDistance,
          colorize,
          "--dictionary", conversionPath,
          "--source-format", conversionFormat,
          "--algorithm", algorithm,
          "--query", QUERY_TERM_1, QUERY_TERM_2))
        .succeeded()
        .printed(output)
          .stripping(RE_COLOR)
          .toStandardOutput();

      // Test adaptive parsing

      assertThat(
        exec(
          isSorted,
          includeDistance,
          colorize,
          "--dictionary", conversionPath,
          "--algorithm", algorithm,
          "--query", QUERY_TERM_1, QUERY_TERM_2))
        .succeeded()
        .printed(output)
          .stripping(RE_COLOR)
          .toStandardOutput();
    }
    finally {
      Files.delete(conversionPath);
      Files.delete(dictionaryPath);
    }
  }

  private Path tmp(final String prefix, final String suffix) throws IOException {
    final Path tmp = Files.createTempFile(prefix, suffix);
    tmp.toFile().deleteOnExit();
    return tmp;
  }

  private Process exec(
      final boolean isSorted,
      final boolean includeDistance,
      final boolean colorize,
      final Object... args) throws IOException, InterruptedException {
    final List<String> command = new LinkedList<>();
    command.add(
      String.format(
        "%s/build/install/liblevenshtein-java-cli/bin/liblevenshtein-java-cli",
        System.getProperty("user.dir")));
    for (final Object arg : args) {
      command.add(arg.toString());
    }
    if (isSorted) {
      command.add("--is-sorted");
    }
    if (includeDistance) {
      command.add("--include-distance");
    }
    if (colorize) {
      command.add("--colorize");
    }
    final ProcessBuilder builder = new ProcessBuilder(command);
    final Map<String, String> env = builder.environment();
    env.put("LIBLEVENSHTEIN_JAVA_CLI_OPTS", "-DLOGGING=OFF");
    log.info("Executing [{}]", SPACES.join(builder.command()));
    final Process proc = builder.start();
    proc.waitFor();
    return proc;
  }
}
