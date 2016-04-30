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

import lombok.extern.slf4j.Slf4j;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.github.liblevenshtein.assertion.ProcessAssertions;
import static com.github.liblevenshtein.assertion.ProcessAssertions.assertThat;

@Slf4j
public class CommandLineInterfaceIntegTest {

  private static final String QUERY_TERM_1 = "fro";

  private static final String QUERY_TERM_2 = "eb";

  private static final String STANDARD_OUTPUT_WITH_DISTANCES =
    "+-------------------------------------------------------------------------------\n"
  + "| Spelling Candidates for Query Term: \"fro\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| d(\"fro\", \"do\") = [2]\n"
  + "| d(\"fro\", \"to\") = [2]\n"
  + "| d(\"fro\", \"for\") = [2]\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| Spelling Candidates for Query Term: \"eb\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| d(\"eb\", \"I\") = [2]\n"
  + "| d(\"eb\", \"a\") = [2]\n"
  + "| d(\"eb\", \"as\") = [2]\n"
  + "| d(\"eb\", \"at\") = [2]\n"
  + "| d(\"eb\", \"be\") = [2]\n"
  + "| d(\"eb\", \"do\") = [2]\n"
  + "| d(\"eb\", \"he\") = [2]\n"
  + "| d(\"eb\", \"in\") = [2]\n"
  + "| d(\"eb\", \"it\") = [2]\n"
  + "| d(\"eb\", \"of\") = [2]\n"
  + "| d(\"eb\", \"on\") = [2]\n"
  + "| d(\"eb\", \"to\") = [2]\n";

  private static final String STANDARD_OUTPUT_WITHOUT_DISTANCES =
    "+-------------------------------------------------------------------------------\n"
  + "| Spelling Candidates for Query Term: \"fro\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| \"fro\" ~ \"do\"\n"
  + "| \"fro\" ~ \"to\"\n"
  + "| \"fro\" ~ \"for\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| Spelling Candidates for Query Term: \"eb\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| \"eb\" ~ \"I\"\n"
  + "| \"eb\" ~ \"a\"\n"
  + "| \"eb\" ~ \"as\"\n"
  + "| \"eb\" ~ \"at\"\n"
  + "| \"eb\" ~ \"be\"\n"
  + "| \"eb\" ~ \"do\"\n"
  + "| \"eb\" ~ \"he\"\n"
  + "| \"eb\" ~ \"in\"\n"
  + "| \"eb\" ~ \"it\"\n"
  + "| \"eb\" ~ \"of\"\n"
  + "| \"eb\" ~ \"on\"\n"
  + "| \"eb\" ~ \"to\"\n";

  private static final String TRANSPOSITION_OUTPUT_WITH_DISTANCES =
    "+-------------------------------------------------------------------------------\n"
  + "| Spelling Candidates for Query Term: \"fro\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| d(\"fro\", \"do\") = [2]\n"
  + "| d(\"fro\", \"to\") = [2]\n"
  + "| d(\"fro\", \"for\") = [1]\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| Spelling Candidates for Query Term: \"eb\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| d(\"eb\", \"I\") = [2]\n"
  + "| d(\"eb\", \"a\") = [2]\n"
  + "| d(\"eb\", \"as\") = [2]\n"
  + "| d(\"eb\", \"at\") = [2]\n"
  + "| d(\"eb\", \"be\") = [1]\n"
  + "| d(\"eb\", \"do\") = [2]\n"
  + "| d(\"eb\", \"he\") = [2]\n"
  + "| d(\"eb\", \"in\") = [2]\n"
  + "| d(\"eb\", \"it\") = [2]\n"
  + "| d(\"eb\", \"of\") = [2]\n"
  + "| d(\"eb\", \"on\") = [2]\n"
  + "| d(\"eb\", \"to\") = [2]\n";

  private static final String TRANSPOSITION_OUTPUT_WITHOUT_DISTANCES =
    "+-------------------------------------------------------------------------------\n"
  + "| Spelling Candidates for Query Term: \"fro\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| \"fro\" ~ \"do\"\n"
  + "| \"fro\" ~ \"to\"\n"
  + "| \"fro\" ~ \"for\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| Spelling Candidates for Query Term: \"eb\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| \"eb\" ~ \"I\"\n"
  + "| \"eb\" ~ \"a\"\n"
  + "| \"eb\" ~ \"as\"\n"
  + "| \"eb\" ~ \"at\"\n"
  + "| \"eb\" ~ \"be\"\n"
  + "| \"eb\" ~ \"do\"\n"
  + "| \"eb\" ~ \"he\"\n"
  + "| \"eb\" ~ \"in\"\n"
  + "| \"eb\" ~ \"it\"\n"
  + "| \"eb\" ~ \"of\"\n"
  + "| \"eb\" ~ \"on\"\n"
  + "| \"eb\" ~ \"to\"\n";

  private static final String MERGE_AND_SPLIT_OUTPUT_WITH_DISTANCES =
    "+-------------------------------------------------------------------------------\n"
  + "| Spelling Candidates for Query Term: \"fro\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| d(\"fro\", \"I\") = [2]\n"
  + "| d(\"fro\", \"a\") = [2]\n"
  + "| d(\"fro\", \"as\") = [2]\n"
  + "| d(\"fro\", \"at\") = [2]\n"
  + "| d(\"fro\", \"be\") = [2]\n"
  + "| d(\"fro\", \"do\") = [1]\n"
  + "| d(\"fro\", \"he\") = [2]\n"
  + "| d(\"fro\", \"in\") = [2]\n"
  + "| d(\"fro\", \"it\") = [2]\n"
  + "| d(\"fro\", \"of\") = [2]\n"
  + "| d(\"fro\", \"on\") = [2]\n"
  + "| d(\"fro\", \"to\") = [1]\n"
  + "| d(\"fro\", \"and\") = [2]\n"
  + "| d(\"fro\", \"for\") = [2]\n"
  + "| d(\"fro\", \"not\") = [2]\n"
  + "| d(\"fro\", \"the\") = [2]\n"
  + "| d(\"fro\", \"you\") = [2]\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| Spelling Candidates for Query Term: \"eb\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| d(\"eb\", \"I\") = [1]\n"
  + "| d(\"eb\", \"a\") = [1]\n"
  + "| d(\"eb\", \"as\") = [2]\n"
  + "| d(\"eb\", \"at\") = [2]\n"
  + "| d(\"eb\", \"be\") = [2]\n"
  + "| d(\"eb\", \"do\") = [2]\n"
  + "| d(\"eb\", \"he\") = [2]\n"
  + "| d(\"eb\", \"in\") = [2]\n"
  + "| d(\"eb\", \"it\") = [2]\n"
  + "| d(\"eb\", \"of\") = [2]\n"
  + "| d(\"eb\", \"on\") = [2]\n"
  + "| d(\"eb\", \"to\") = [2]\n"
  + "| d(\"eb\", \"and\") = [2]\n"
  + "| d(\"eb\", \"for\") = [2]\n"
  + "| d(\"eb\", \"not\") = [2]\n"
  + "| d(\"eb\", \"the\") = [2]\n"
  + "| d(\"eb\", \"you\") = [2]\n"
  + "| d(\"eb\", \"have\") = [2]\n"
  + "| d(\"eb\", \"that\") = [2]\n"
  + "| d(\"eb\", \"with\") = [2]\n";

  private static final String MERGE_AND_SPLIT_OUTPUT_WITHOUT_DISTANCES =
    "+-------------------------------------------------------------------------------\n"
  + "| Spelling Candidates for Query Term: \"fro\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| \"fro\" ~ \"I\"\n"
  + "| \"fro\" ~ \"a\"\n"
  + "| \"fro\" ~ \"as\"\n"
  + "| \"fro\" ~ \"at\"\n"
  + "| \"fro\" ~ \"be\"\n"
  + "| \"fro\" ~ \"do\"\n"
  + "| \"fro\" ~ \"he\"\n"
  + "| \"fro\" ~ \"in\"\n"
  + "| \"fro\" ~ \"it\"\n"
  + "| \"fro\" ~ \"of\"\n"
  + "| \"fro\" ~ \"on\"\n"
  + "| \"fro\" ~ \"to\"\n"
  + "| \"fro\" ~ \"and\"\n"
  + "| \"fro\" ~ \"for\"\n"
  + "| \"fro\" ~ \"not\"\n"
  + "| \"fro\" ~ \"the\"\n"
  + "| \"fro\" ~ \"you\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| Spelling Candidates for Query Term: \"eb\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| \"eb\" ~ \"I\"\n"
  + "| \"eb\" ~ \"a\"\n"
  + "| \"eb\" ~ \"as\"\n"
  + "| \"eb\" ~ \"at\"\n"
  + "| \"eb\" ~ \"be\"\n"
  + "| \"eb\" ~ \"do\"\n"
  + "| \"eb\" ~ \"he\"\n"
  + "| \"eb\" ~ \"in\"\n"
  + "| \"eb\" ~ \"it\"\n"
  + "| \"eb\" ~ \"of\"\n"
  + "| \"eb\" ~ \"on\"\n"
  + "| \"eb\" ~ \"to\"\n"
  + "| \"eb\" ~ \"and\"\n"
  + "| \"eb\" ~ \"for\"\n"
  + "| \"eb\" ~ \"not\"\n"
  + "| \"eb\" ~ \"the\"\n"
  + "| \"eb\" ~ \"you\"\n"
  + "| \"eb\" ~ \"have\"\n"
  + "| \"eb\" ~ \"that\"\n"
  + "| \"eb\" ~ \"with\"\n";

  private static final String HELP_TEXT =
    "usage: liblevenshtein-java-cli [-a <ALGORITHM>] [--colorize] -d <PATH|URI>\n"
  + "       [-h] [-i] [-m <INTEGER>] [-q <STRING> <...>] [-s] [--serialize\n"
  + "       <PATH>] [--source-format <FORMAT>] [--target-format <FORMAT>]\n"
  + "\n"
  + "Command-Line Interface to liblevenshtein (Java)\n"
  + "\n"
  + "<FORMAT> specifies the serialization format of the dictionary,\n"
  + "and may be one of the following:\n"
  + "  1. PROTOBUF\n"
  + "     - (de)serialize the dictionary as a protobuf stream.\n"
  + "     - This is the preferred format.\n"
  + "     - See: https://developers.google.com/protocol-buffers/\n"
  + "  2. BYTECODE\n"
  + "     - (de)serialize the dictionary as a Java, bytecode stream.\n"
  + "  3. PLAIN_TEXT\n"
  + "     - (de)serialize the dictionary as a plain text file.\n"
  + "     - Terms are delimited by newlines.\n"
  + "\n"
  + "<ALGORITHM> specifies the Levenshtein algorithm to use for\n"
  + "querying-against the dictionary, and may be one of the following:\n"
  + "  1. STANDARD\n"
  + "     - Use the standard, Levenshtein distance which considers the\n"
  + "     following elementary operations:\n"
  + "       o Insertion\n"
  + "       o Deletion\n"
  + "       o Substitution\n"
  + "     - An elementary operation is an operation that incurs a penalty of\n"
  + "     one unit.\n"
  + "  2. TRANSPOSITION\n"
  + "     - Extend the standard, Levenshtein distance to include transpositions\n"
  + "     as elementary operations.\n"
  + "       o A transposition is a swapping of two, consecutive characters as\n"
  + "       follows: ba -> ab\n"
  + "       o With the standard distance, this would require at least two\n"
  + "       operations:\n"
  + "         + An insertion and a deletion\n"
  + "         + A deletion and an insertion\n"
  + "         + Two substitutions\n"
  + "  3. MERGE_AND_SPLIT\n"
  + "     - Extend the standard, Levenshtein distance to include merges and\n"
  + "     splits as elementary operations.\n"
  + "       o A merge takes two characters and merges them into a single one.\n"
  + "         + For example: ab -> c\n"
  + "       o A split takes a single character and splits it into two others\n"
  + "         + For example: a -> bc\n"
  + "       o With the standard distance, these would require at least two\n"
  + "       operations:\n"
  + "         + Merge:\n"
  + "           > A deletion and a substitution\n"
  + "           > A substitution and a deletion\n"
  + "         + Split:\n"
  + "           > An insertion and a substitution\n"
  + "           > A substitution and an insertion\n"
  + "\n"
  + " -a,--algorithm <ALGORITHM>    Levenshtein algorithm to use (Default:\n"
  + "                               TRANSPOSITION)\n"
  + "    --colorize                 Colorize output\n"
  + " -d,--dictionary <PATH|URI>    Filesystem path or Java-compatible URI to a\n"
  + "                               dictionary of terms\n"
  + " -h,--help                     print this help text\n"
  + " -i,--include-distance         Include the Levenshtein distance with each\n"
  + "                               spelling candidate (Default: false)\n"
  + " -m,--max-distance <INTEGER>   Maximun, Levenshtein distance a spelling\n"
  + "                               candidatemay be from the query term\n"
  + "                               (Default: 2)\n"
  + " -q,--query <STRING> <...>     Terms to query against the dictionary.  You\n"
  + "                               may specify multiple terms.\n"
  + " -s,--is-sorted                Specifies that the dictionary is sorted\n"
  + "                               lexicographically, in ascending order\n"
  + "                               (Default: false)\n"
  + "    --serialize <PATH>         Path to save the serialized dictionary\n"
  + "    --source-format <FORMAT>   Format of the source dictionary (Default:\n"
  + "                               adaptively-try each format until one works)\n"
  + "    --target-format <FORMAT>   Format of the serialized dictionary\n"
  + "                               (Default: PROTOBUF)\n"
  + "\n"
  + "Example: liblevenshtein-java-cli \\\n"
  + "  --algorithm TRANSPOSITION \\\n"
  + "  --max-distance 2 \\\n"
  + "  --include-distance \\\n"
  + "  --query mispelled mispelling \\\n"
  + "  --colorize\n";

  /** Strips syntax coloring from output. */
  private static final Pattern RE_COLOR =
    Pattern.compile("\\e\\[(?:\\d+(?:;\\d+)*)?m");

  private static final Joiner SPACES = Joiner.on(" ");

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

  @SuppressWarnings("unchecked")
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

      // add option for sorted

    final Path dictionaryPath = tmp("dictionary-", "." + dictionaryFormat);
    final Path conversionPath = tmp("dictionary-", "." + conversionFormat);

    try {
      ProcessAssertions assertions;

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
      assertions =
        assertThat(
          exec(
            isSorted,
            includeDistance,
            colorize,
            "--dictionary", dictionaryPath,
            "--source-format", dictionaryFormat,
            "--serialize", conversionPath,
            "--target-format", conversionFormat));

      if (isSorted && "PLAIN_TEXT".equals(dictionaryFormat)) {
        assertions.failed();
        return; // the dictionary was not converted, so don't proceed
      }
      else {
        assertions.succeeded();
      }

      // Test formatted parsing

      assertions =
        assertThat(
          exec(
            isSorted,
            includeDistance,
            colorize,
            "--dictionary", conversionPath,
            "--source-format", conversionFormat,
            "--algorithm", algorithm,
            "--query", QUERY_TERM_1, QUERY_TERM_2));

      if (isSorted && "PLAIN_TEXT".equals(conversionFormat)) {
        assertions.failed();
      }
      else {
        assertions.succeeded()
          .printed(output)
            .stripping(RE_COLOR)
            .toStandardOutput();
      }

      // Test adaptive parsing

      assertions =
        assertThat(
          exec(
            isSorted,
            includeDistance,
            colorize,
            "--dictionary", conversionPath,
            "--algorithm", algorithm,
            "--query", QUERY_TERM_1, QUERY_TERM_2));

      if (isSorted && "PLAIN_TEXT".equals(conversionFormat)) {
        assertions.failed();
      }
      else {
        assertions.succeeded()
          .printed(output)
            .stripping(RE_COLOR)
            .toStandardOutput();
      }
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
