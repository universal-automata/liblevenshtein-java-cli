package com.github.liblevenshtein;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;

import lombok.extern.slf4j.Slf4j;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.github.liblevenshtein.assertion.ProcessAssertions.assertThat;

@Slf4j
public class CommandLineInterfaceIntegTest {

  private static final String OUTPUT_WITHOUT_DISTANCES =
    "+-------------------------------------------------------------------------------\n"
  + "| Spelling Candidates for Query Term: \"mispelled\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| \"mispelled\" ~ \"spelled\"\n"
  + "| \"mispelled\" ~ \"impelled\"\n"
  + "| \"mispelled\" ~ \"dispelled\"\n"
  + "| \"mispelled\" ~ \"miscalled\"\n"
  + "| \"mispelled\" ~ \"respelled\"\n"
  + "| \"mispelled\" ~ \"misspelled\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| Spelling Candidates for Query Term: \"mispelling\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| \"mispelling\" ~ \"spelling\"\n"
  + "| \"mispelling\" ~ \"impelling\"\n"
  + "| \"mispelling\" ~ \"dispelling\"\n"
  + "| \"mispelling\" ~ \"misbilling\"\n"
  + "| \"mispelling\" ~ \"miscalling\"\n"
  + "| \"mispelling\" ~ \"misdealing\"\n"
  + "| \"mispelling\" ~ \"respelling\"\n"
  + "| \"mispelling\" ~ \"misspelling\"\n"
  + "| \"mispelling\" ~ \"misspellings\"\n";

  private static final String OUTPUT_WITH_DISTANCES =
    "+-------------------------------------------------------------------------------\n"
  + "| Spelling Candidates for Query Term: \"mispelled\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| d(\"mispelled\", \"spelled\") = [2]\n"
  + "| d(\"mispelled\", \"impelled\") = [2]\n"
  + "| d(\"mispelled\", \"dispelled\") = [1]\n"
  + "| d(\"mispelled\", \"miscalled\") = [2]\n"
  + "| d(\"mispelled\", \"respelled\") = [2]\n"
  + "| d(\"mispelled\", \"misspelled\") = [1]\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| Spelling Candidates for Query Term: \"mispelling\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| d(\"mispelling\", \"spelling\") = [2]\n"
  + "| d(\"mispelling\", \"impelling\") = [2]\n"
  + "| d(\"mispelling\", \"dispelling\") = [1]\n"
  + "| d(\"mispelling\", \"misbilling\") = [2]\n"
  + "| d(\"mispelling\", \"miscalling\") = [2]\n"
  + "| d(\"mispelling\", \"misdealing\") = [2]\n"
  + "| d(\"mispelling\", \"respelling\") = [2]\n"
  + "| d(\"mispelling\", \"misspelling\") = [1]\n"
  + "| d(\"mispelling\", \"misspellings\") = [2]\n";

  private static final String HELP_TEXT =
    "usage: liblevenshtein-java-cli [-a] [--colorize] [-d <PATH|URI>] [-h] [-i]\n"
  + "       [-m <INTEGER>] [-q <STRING> <STRING> <...>] [-s] [--serialize\n"
  + "       <PATH>] [--source-format <PROTOBUF|BYTECODE|PLAIN_TEXT>]\n"
  + "       [--target-format <PROTOBUF|BYTECODE|PLAIN_TEXT>]\n"
  + "Command-Line Interface to liblevenshtein (Java)\n"
  + "\n"
  + " -a,--algorithm                                      Levenshtein algorithm\n"
  + "                                                     to use (Default:\n"
  + "                                                     TRANSPOSITION)\n"
  + "    --colorize                                       Colorize output\n"
  + " -d,--dictionary <PATH|URI>                          Filesystem path or\n"
  + "                                                     Java-compatible URI\n"
  + "                                                     to a dictionary of\n"
  + "                                                     terms (Default:\n"
  + "                                                     jar:file:///home/dylo\n"
  + "                                                     n/Workspace/liblevens\n"
  + "                                                     htein-java/java-cli/b\n"
  + "                                                     uild/install/libleven\n"
  + "                                                     shtein-java-cli/lib/l\n"
  + "                                                     iblevenshtein-java-cl\n"
  + "                                                     i-2.2.1.jar!/wordsEn.\n"
  + "                                                     txt)\n"
  + " -h,--help                                           print this help text\n"
  + " -i,--include-distance                               Include the\n"
  + "                                                     Levenshtein distance\n"
  + "                                                     with each spelling\n"
  + "                                                     candidate (Default:\n"
  + "                                                     false)\n"
  + " -m,--max-distance <INTEGER>                         Maximun, Levenshtein\n"
  + "                                                     distance a spelling\n"
  + "                                                     candidatemay be from\n"
  + "                                                     the query term\n"
  + "                                                     (Default: 2)\n"
  + " -q,--query <STRING> <STRING> <...>                  Terms to query\n"
  + "                                                     against the\n"
  + "                                                     dictionary\n"
  + " -s,--is-sorted                                      Specifies that the\n"
  + "                                                     dictionary is sorted\n"
  + "                                                     lexicographically, in\n"
  + "                                                     ascending order\n"
  + "                                                     (Default: false)\n"
  + "    --serialize <PATH>                               Path to save the\n"
  + "                                                     serialized dictionary\n"
  + "    --source-format <PROTOBUF|BYTECODE|PLAIN_TEXT>   Format of the source\n"
  + "                                                     dictionary (Default:\n"
  + "                                                     adaptive)\n"
  + "    --target-format <PROTOBUF|BYTECODE|PLAIN_TEXT>   Format of the\n"
  + "                                                     serialized dictionary\n"
  + "                                                     (Default: PROTOBUF)\n"
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
    assertThat(exec(false))
      .failed()
      .printed(HELP_TEXT)
        .toStandardOutput();
    assertThat(exec(false, "-h"))
      .succeeded()
      .printed(HELP_TEXT)
        .toStandardOutput();
    assertThat(exec(false, "--help"))
      .succeeded()
      .printed(HELP_TEXT)
        .toStandardOutput();
    assertThat(exec(false, "--error"))
      .failed()
      .printed(HELP_TEXT)
        .toStandardOutput();
    assertThat(exec(false, "--query", "foo", "-h"))
      .succeeded()
      .printed(HELP_TEXT)
        .toStandardOutput();
    assertThat(exec(false, "--query", "foo", "--help"))
      .succeeded()
      .printed(HELP_TEXT)
        .toStandardOutput();
  }

  @DataProvider(name="includeDistanceProvider")
  public Object[][] includeDistanceProvider() {
    return new Object[][] {
      {false, OUTPUT_WITHOUT_DISTANCES},
      {true, OUTPUT_WITH_DISTANCES},
    };
  }

  @SuppressWarnings("unchecked")
  @DataProvider(name="sourceToTargetProvider")
  public Object[][] sourceToTargetProvider() {
    try {
      final String[] formats = {"PLAIN_TEXT", "PROTOBUF", "BYTECODE"};
      final Object[][] includeDistances = includeDistanceProvider();

      final List<Object[]> provider =
        new ArrayList<>(includeDistances.length * formats.length * formats.length);

      for (final Object[] params : includeDistances) {
        final boolean includeDistance = (Boolean) params[0];
        final String output = (String) params[1];
        for (final String sourceFormat : formats) {
          for (final String targetFormat : formats) {
            provider.add(new Object[] {
              includeDistance,
              output,
              sourceFormat,
              targetFormat,
            });
          }
        }
      }

      return (Object[][]) (Object) provider.toArray(new Object[0][0]);
    }
    catch (final Throwable thrown) {
      log.error("Failed to generate sourceToTargetProvider", thrown);
      throw thrown;
    }
  }

  @Test(dataProvider="includeDistanceProvider")
  public void testWithDistance(
      final boolean includeDistance,
      final String output)
      throws IOException, InterruptedException {

    assertThat(exec(includeDistance,
          "--query",
          "mispelled",
          "mispelling"))
      .succeeded()
      .printed(output)
        .toStandardOutput();

    assertThat(exec(includeDistance,
          "--query",
          "mispelled",
          "mispelling",
          "--colorize"))
      .succeeded()
      .printed(output)
        .stripping(RE_COLOR) // strip syntax info
        .toStandardOutput();
  }

  @Test(dataProvider="sourceToTargetProvider")
  public void testWithDistanceAndDictionaryFormatAndConversionFormat(
      final boolean includeDistance,
      final String output,
      final String dictionaryFormat,
      final String conversionFormat)
      throws IOException, InterruptedException {

    final Path dictionaryPath = tmp("dictionary-", "." + dictionaryFormat);
    final Path conversionPath = tmp("dictionary-", "." + conversionFormat);

    try {
      // Pull the dictionary from the Jar
      assertThat(exec(includeDistance,
            "--serialize", dictionaryPath,
            "--target-format", dictionaryFormat))
        .succeeded();

      // Convert the dictionary to some other format
      assertThat(exec(includeDistance,
            "--dictionary", dictionaryPath,
            "--source-format", dictionaryFormat,
            "--serialize", conversionPath,
            "--target-format", conversionFormat))
        .succeeded();

      // Test formatted parsing

      assertThat(exec(includeDistance,
            "--dictionary", conversionPath,
            "--source-format", conversionFormat,
            "--query",
            "mispelled",
            "mispelling"))
        .succeeded()
        .printed(output)
          .toStandardOutput();

      assertThat(exec(includeDistance,
            "--dictionary", conversionPath,
            "--source-format", conversionFormat,
            "--query",
            "mispelled",
            "mispelling",
            "--colorize"))
        .succeeded()
        .printed(output)
          .stripping(RE_COLOR) // strip syntax info
          .toStandardOutput();

      // Test adaptive parsing

      assertThat(exec(includeDistance,
            "--dictionary", conversionPath,
            "--query",
            "mispelled",
            "mispelling"))
        .succeeded()
        .printed(output)
          .toStandardOutput();

      assertThat(exec(includeDistance,
            "--dictionary", conversionPath,
            "--query",
            "mispelled",
            "mispelling",
            "--colorize"))
        .succeeded()
        .printed(output)
          .stripping(RE_COLOR) // strip syntax info
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
      final boolean includeDistance,
      final Object... args) throws IOException, InterruptedException {
    final List<String> command = new LinkedList<>();
    command.add(
      String.format(
        "%s/build/install/liblevenshtein-java-cli/bin/liblevenshtein-java-cli",
        System.getProperty("user.dir")));
    for (final Object arg : args) {
      command.add(arg.toString());
    }
    if (includeDistance) {
      command.add("--include-distance");
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
