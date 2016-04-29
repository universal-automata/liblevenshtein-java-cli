package com.github.liblevenshtein;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.apache.commons.cli.AlreadySelectedException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;

import org.apache.commons.lang3.StringEscapeUtils;

import com.google.common.base.Joiner;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.ExtensionMethod;
import lombok.extern.slf4j.Slf4j;

import com.github.dylon.liblevenshtein.collection.dawg.DawgNode;
import com.github.dylon.liblevenshtein.collection.dawg.SortedDawg;
import com.github.dylon.liblevenshtein.collection.dawg.factory.DawgNodeFactory;
import com.github.dylon.liblevenshtein.collection.dawg.factory.PrefixFactory;
import com.github.dylon.liblevenshtein.collection.dawg.factory.TransitionFactory;
import com.github.dylon.liblevenshtein.levenshtein.Algorithm;
import com.github.dylon.liblevenshtein.levenshtein.Candidate;
import com.github.dylon.liblevenshtein.levenshtein.ITransducer;
import com.github.dylon.liblevenshtein.levenshtein.factory.TransducerBuilder;
import com.github.dylon.liblevenshtein.serialization.BytecodeSerializer;
import com.github.dylon.liblevenshtein.serialization.ProtobufSerializer;
import com.github.dylon.liblevenshtein.serialization.Serializer;

import com.github.liblevenshtein.util.HighlightUtils;

/**
 * Command-line interface to liblevenshtein (Java).
 */
@Slf4j
@ExtensionMethod({HighlightUtils.class})
public class CommandLineInterface implements Runnable {

  /**
   * Heuristic to distinguish between URIs and filesystem paths.
   */
  private static final Pattern RE_PROTO =
    Pattern.compile("^(?:[a-z]+:)*[a-z]+://.*$");

  /**
   * Process exit code for success.
   */
  public static final int EXIT_SUCCESS = 0;

  /**
   * Process exit code for a handled exception.
   */
  public static final int EXIT_ERROR = 1;

  /**
   * Process exit code for an unhandled exception.
   */
  public static final int EXIT_UNHANDLED = 2;

  /**
   * Default, Levenshtein algorithm to use for querying the dictionary.
   */
  private static final Algorithm DEFAULT_ALGORITHM = Algorithm.TRANSPOSITION;

  /**
   * Default, number of spelling errors to accept when querying the dictionary.
   */
  private static final int DEFAULT_MAX_DISTANCE = 2;

  /**
   * Default, indicator whether to include the number of errors with the
   * spelling candidates.
   */
  private static final boolean DEFAULT_INCLUDE_DISTANCE = true;

  /**
   * Default format for serializing dictionaries.
   */
  private static final SerializationFormat DEFAULT_FORMAT =
    SerializationFormat.PROTOBUF;

  /**
   * Joins elements with commas.
   */
  private static final Joiner COMMAS = Joiner.on(", ");

  /**
   * Joins elements with pipes.
   */
  private static final Joiner PIPES = Joiner.on("|");

  /**
   * Joins elements with newlines.
   */
  private static final Joiner NEWLINES = Joiner.on("\n");

  /**
   * Command-line parameters of this action.
   */
  private final CommandLine cli;

  /**
   * Constructs a new command-line interface with the arguments.
   * @param args Command-line arguments
   */
  public CommandLineInterface(final String[] args) {
    this.cli = parseCLI(args);
  }

  /**
   * Parses the command-line parameters into options.
   * @param args Command-lien arguments for this action.
   * @return {@link CommandLine} for this action.
   */
  private CommandLine parseCLI(final String[] args) {
    try {
      log.info("Parsing command-line args [{}]", COMMAS.join(args));
      if (0 == args.length) {
        printHelp(EXIT_ERROR);
      }
      for (final String arg : args) {
        if ("--help".equals(arg) || "-h".equals(arg)) {
          printHelp(EXIT_SUCCESS);
        }
      }
      final DefaultParser parser = new DefaultParser();
      final CommandLine cli = parser.parse(options(), args);
      return cli;
    }
    catch (final AlreadySelectedException exception) {
      final String message =
        String.format("Option specified multiple times [%s]",
          exception.getOption());
      handleParseException(message, exception);
    }
    catch (final MissingArgumentException exception) {
      final String message =
        String.format("Missing argument for option [%s]",
          exception.getOption());
      handleParseException(message, exception);
    }
    catch (final MissingOptionException exception) {
      final String message =
        String.format("The following required options were missing: %s",
          COMMAS.join(exception.getMissingOptions()));
      handleParseException(message, exception);
    }
    catch (final UnrecognizedOptionException exception) {
      final String message =
        String.format("Uncrecognized option [%s]", exception.getOption());
      handleParseException(message, exception);
    }
    catch (final ParseException exception) {
      final String message =
        String.format("Unexpected exception while parsing options [%s]",
          COMMAS.join(args));
      handleParseException(message, exception);
    }

    throw new IllegalStateException("Should be unreachable");
  }

  /**
   * Stream to the dictionary to query against.  This may be any valid,
   * filesystem path or Java-compatible URI (such as a remote dictionary, Jar
   * resource, etc.).
   * @return Stream to the dictionary to query against.
   */
  private InputStream dictionary() throws IOException {
    final String path = cli.getOptionValue("dictionary");

    try {
      if (null == path && 0 != System.in.available()) {
        return System.in;
      }
    }
    catch (final IOException exception) {
      log.warn("Cannot read from <STDIN>");
    }

    try {
      final URI uri = RE_PROTO.matcher(path).matches()
        ? new URI(path)
        : Paths.get(path).toUri();

      return uri.toURL().openStream();
    }
    catch (final Exception exception) {
      final String message =
        String.format("Failed to build dictionary from [%s]", path);
      throw new IllegalArgumentException(message, exception);
    }
  }

  /**
   * Specifies whether the dictionary is sorted (saves work if it is).
   * @return Whether the dictionary is sorted.
   */
  private boolean isSorted() {
    return cli.hasOption("is-sorted");
  }

  /**
   * Levenshtein algorithm to use while querying the dictionary.
   * @return Levenshtein algorithm to use while querying the dictionary.
   */
  private Algorithm algorithm() {
    final String name = cli.getOptionValue("algorithm");

    if (null == name) {
      return DEFAULT_ALGORITHM;
    }

    for (final Algorithm algorithm : Algorithm.values()) {
      if (algorithm.name().equals(name)) {
        return algorithm;
      }
    }

    final String message =
      String.format("Unknown algorithm [%s], expected one of [%s]",
        name, COMMAS.join(Algorithm.values()));
    throw new IllegalArgumentException(message);
  }

  /**
   * Maximum-allowed, Levenshtein distance a spelling candidate may be from its
   * query term.
   * @return Maximum, Levenshtein distance of spelling candidates.
   */
  private int maxDistance() {
    final String maxDistance = cli.getOptionValue("max-distance");

    if (null == maxDistance) {
      return DEFAULT_MAX_DISTANCE;
    }

    try {
      return Integer.parseInt(maxDistance);
    }
    catch (final NumberFormatException exception) {
      final String message =
        String.format("Expeted an integer for max-distance, but received [%s]",
          maxDistance);
      throw new IllegalArgumentException(message, exception);
    }
  }

  /**
   * Whether to include the number of errors from each query term, with the
   * spelling candidates.
   * @return Whether to include the Levenshtein distance.
   */
  private boolean includeDistance() {
    return cli.hasOption("include-distance");
  }

  /**
   * Terms to query against the dictionary.
   * @return Terms to query against the dictionary.
   */
  private List<String> queryTerms() {
    if (cli.hasOption("query")) {
      return Arrays.asList(cli.getOptionValues("query"));
    }
    return Arrays.asList();
  }

  /**
   * Where to serialize the dictionary.  This will be null if the dictionary
   * should not be serialzied.
   * @return Where to serialize the dictionary.
   */
  private Path serializationPath() {
    final String serializationPath = cli.getOptionValue("serialize");
    if (null == serializationPath) {
      return null;
    }
    return Paths.get(serializationPath);
  }

  /**
   * Returns the source, serialization format for dictionaries (or null, if no
   * source format was specified).
   * @return Target, serialization format for dictionaries.
   */
  private SerializationFormat sourceFormat() {
    final String sourceFormat = cli.getOptionValue("source-format");
    if (null == sourceFormat) {
      return null;
    }
    return SerializationFormat.valueOf(sourceFormat);
  }

  /**
   * Returns the target, serialization format for dictionaries
   * (or {@link #DEFAULT_FORMAT}, if no target format was specified).
   * @return Target, serialization format for dictionaries.
   */
  private SerializationFormat targetFormat() {
    final String targetFormat = cli.getOptionValue("target-format");
    if (null == targetFormat) {
      return DEFAULT_FORMAT;
    }
    return SerializationFormat.valueOf(targetFormat);
  }

  /**
   * Whether to colorize the output.
   * @return Whether to colorize the output.
   */
  private boolean colorize() {
    return cli.hasOption("colorize");
  }

  /**
   * Command-line options for this action.
   * @return Command-line options.
   */
  private Options options() {
    final Options options = new Options();
    options.addOption(
      Option.builder("h")
        .longOpt("help")
        .desc("print this help text")
        .build());
    options.addOption(
      Option.builder("d")
        .longOpt("dictionary")
        .argName("PATH|URI")
        .desc("Filesystem path or Java-compatible URI to a dictionary of terms")
        .hasArg()
        .required()
        .build());
    options.addOption(
      Option.builder("s")
        .longOpt("is-sorted")
        .desc("Specifies that the dictionary is sorted lexicographically, in "
          + "ascending order (Default: false)")
        .build());
    options.addOption(
      Option.builder("a")
        .longOpt("algorithm")
        .argName("ALGORITHM")
        .desc(String.format("Levenshtein algorithm to use (Default: %s)",
          DEFAULT_ALGORITHM))
        .hasArg()
        .build());
    options.addOption(
      Option.builder("m")
        .longOpt("max-distance")
        .argName("INTEGER")
        .desc(String.format("Maximun, Levenshtein distance a spelling candidate"
          + "may be from the query term (Default: %d)", DEFAULT_MAX_DISTANCE))
        .hasArg()
        .build());
    options.addOption(
      Option.builder("i")
        .longOpt("include-distance")
        .desc("Include the Levenshtein distance with each spelling candidate "
          + "(Default: false)")
        .build());
    options.addOption(
      Option.builder("q")
        .longOpt("query")
        .argName("STRING> <...")
        .desc("Terms to query against the dictionary.  You may specify multiple terms.")
        .hasArgs()
        .build());
    options.addOption(
      Option.builder()
        .longOpt("serialize")
        .argName("PATH")
        .desc("Path to save the serialized dictionary")
        .hasArg()
        .build());
    options.addOption(
      Option.builder()
        .longOpt("source-format")
        .argName("FORMAT")
        .desc("Format of the source dictionary (Default: adaptively-try each format until one works)")
        .hasArg()
        .build());
    options.addOption(
      Option.builder()
        .longOpt("target-format")
        .argName("FORMAT")
        .desc(String.format("Format of the serialized dictionary (Default: %s)",
          DEFAULT_FORMAT))
        .hasArg()
        .build());
    options.addOption(
      Option.builder()
        .longOpt("colorize")
        .desc("Colorize output")
        .build());
    return options;
  }

  /**
   * Gracefully-handles exceptions from parsing the command-line paramters.
   * @param message Error message to give the user.
   * @param exception Exception for the message.
   */
  private void handleParseException(
      final String message,
      final ParseException exception) {
    log.error(message, exception);
    printHelp(EXIT_ERROR);
  }

  /**
   * Prints the help text for this action and exits with the given code.
   * @param exitCode Exit code specifying the success of this process.
   */
  private void printHelp(final int exitCode) {
    final HelpFormatter formatter = new HelpFormatter();
    final String name = "liblevenshtein-java-cli";
    final String header = String.format("%s%n%n", NEWLINES.join(
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
      "           > A substitution and an insertion"));
    final String footer =
      String.format(
        "%nExample: %s \\%n"
        + "  --algorithm TRANSPOSITION \\%n"
        + "  --max-distance 2 \\%n"
        + "  --include-distance \\%n"
        + "  --query mispelled mispelling \\%n"
        + "  --colorize", name);
    formatter.printHelp(name, header, options(), footer, true);
    exit(exitCode);
  }

  /**
   * Exits this action with the given code.
   * @param exitCode Exit code specifying the success of this process.
   */
  public void exit(final int exitCode) {
    System.exit(exitCode);
  }

  /**
   * Deserializes the dictionary as the desired format.
   * @param serializer Deserializes the dictionary.
   * @return Deserialized dictionary.
   * @throws Exception When the dictionary cannot be deserialized.
   */
  private SortedDawg deserialize(final Serializer serializer) throws Exception {
    try (final InputStream stream = dictionary()) {
      return serializer.deserialize(SortedDawg.class, stream);
    }
  }

  /**
   * Deserializes the dictionary as a plain text stream (newline-delimited
   * terms).
   * @return Deserialized dictionary.
   * @throws Exception When the dictionary cannot be deserialized.
   */
  private SortedDawg deserializePlainText() throws Exception {
    try (final BufferedReader reader =
        new BufferedReader(
          new InputStreamReader(dictionary(), StandardCharsets.UTF_8))) {

      final Collection<String> terms = isSorted()
        ? new LinkedList<>()
        : new TreeSet<>();

      for (String term = reader.readLine(); null != term; term = reader.readLine()) {
        terms.add(term);
      }

      return new SortedDawg(
        new PrefixFactory<DawgNode>(),
        new DawgNodeFactory(),
        new TransitionFactory<DawgNode>(),
        terms);
    }
  }

  /**
   * Deserialize the dictionary using the specified format.
   * @param format Serialization format of the dictionary stream.
   * @return Dictionary desized using the specified format.
   * @throws Exception When the dictionary cannot be deserialized as the given
   * format.
   */
  private SortedDawg deserialize(final SerializationFormat format) throws Exception {
    switch (format) {
      case PROTOBUF:
        return deserialize(new ProtobufSerializer());
      case PLAIN_TEXT:
        return deserializePlainText();
      case BYTECODE:
        return deserialize(new BytecodeSerializer());
      default:
        final String message = String.format("Unsupported format [%s]", format);
        throw new IllegalArgumentException(message);
    }
  }

  /**
   * Guess the content-type of the dictionary stream.
   * @return Content-type of the dictionary.
   * @throws IOException If the content-type cannot be guessed.
   */
  private String dictionaryContentType() throws IOException {
    Path tmp = null;

    try {
      tmp = Files.createTempFile("dictionary-", ".unknown");
      tmp.toFile().deleteOnExit();

      try (final InputStream stream = dictionary()) {
        Files.copy(stream, tmp, StandardCopyOption.REPLACE_EXISTING);
      }

      // Guess the content-type of the dictionary stream
      return Files.probeContentType(tmp);
    }
    finally {
      if (null != tmp) {
        Files.delete(tmp);
      }
    }
  }

  /**
   * Adaptively-deserializes the dictionary by trying each serializer until one
   * succeeds.
   * @return Dictionary from the first deserializer that succeeds.
   * @throws Exception If the dictionary cannot be deserialized.
   */
  private SortedDawg deserializeAdaptive() throws Exception {
    for (final SerializationFormat format : SerializationFormat.values()) {
      try {
        log.info("Attempting to deserialize dictionary as a [{}] stream", format);
        return deserialize(format);
      }
      catch (final Exception exception) {
        log.warn("Nope, dictionary is not a [{}] stream", format);
      }
    }

    final String message =
      String.format(
        "Cannot read dictionary, which appears to have the content-type [%s].",
          dictionaryContentType());

    throw new IllegalStateException(message);
  }

  /**
   * Builds a new dictionary from the specified stream and whether it is sorted.
   * @return New dictionary, according to command-line arguments.
   * @throws IOException When the dictionary cannot be read from the stream.
   */
  @SuppressFBWarnings("REC_CATCH_EXCEPTION")
  private SortedDawg buildDictionary() throws Exception {
    if (null == sourceFormat()) {
      return deserializeAdaptive();
    }

    try {
      return deserialize(sourceFormat());
    }
    catch (final Exception exception) {
      final String dictionaryContentType = dictionaryContentType();

      if (!dictionaryContentType.equals(sourceFormat().contentType())) {
        log.warn("Serialization format [{}] expects a content-type [{}], but "
            + "the dictionary appears to have the content-type [{}].",
            sourceFormat(), sourceFormat().contentType(),
            dictionaryContentType);
      }

      final String message =
        String.format("Failed to deserialize dictionary as type [%s].",
          sourceFormat());

      throw new IOException(message, exception);
    }
  }

  /**
   * Highlights string quotes.
   * @param buffer Holds messages.
   * @param quote String quote to highlight.
   * @return buffer, for fluency.
   */
  private StringBuilder highlightQuote(
      final StringBuilder buffer,
      final String quote) {
    buffer.foreground(HighlightUtils.MAGENTA).append(quote);
    return buffer;
  }

  /**
   * Highlights strings.
   * @param buffer Holds messages.
   * @param text String text to highlight.
   * @return buffer, for fluency.
   */
  private StringBuilder highlightString(
      final StringBuilder buffer,
      final String text) {
    highlightQuote(buffer, "\"");
    buffer.foreground(HighlightUtils.GREEN).append(text);
    highlightQuote(buffer, "\"");
    return buffer;
  }

  /**
   * Highlights method names.
   * @param buffer Holds messages.
   * @param name Method name to highlight.
   * @return buffer, for fluency.
   */
  private StringBuilder highlightMethodName(
      final StringBuilder buffer,
      final String name) {
    buffer.foreground(HighlightUtils.RED).append(name);
    return buffer;
  }

  /**
   * Highlights parentheses.
   * @param buffer Holds messages.
   * @param paren Parenthesis to highlight.
   * @return buffer, for fluency.
   */
  private StringBuilder highlighParen(
      final StringBuilder buffer,
      final String paren) {
    buffer.foreground(HighlightUtils.WHITE).append(paren);
    return buffer;
  }

  /**
   * Highlights a token separator.
   * @param buffer Holds messages.
   * @param sep Token separator to highlight.
   * @return buffer, for fluency.
   */
  private StringBuilder highlightSeparator(
      final StringBuilder buffer,
      final String sep) {
    buffer.foreground(HighlightUtils.WHITE).append(sep);
    return buffer;
  }

  /**
   * Highlights an operator.
   * @param buffer Holds messages.
   * @param op Operator to highlight.
   * @return buffer, for fluency.
   */
  private StringBuilder highlightOperator(
      final StringBuilder buffer,
      final String op) {
    buffer.foreground(HighlightUtils.WHITE).append(op);
    return buffer;
  }

  /**
   * Highlights a bracket.
   * @param buffer Holds messages.
   * @param bracket Bracket to highlight.
   * @return buffer, for fluency.
   */
  private StringBuilder highlightBracket(
      final StringBuilder buffer,
      final String bracket) {
    buffer.foreground(HighlightUtils.CYAN).append(bracket);
    return buffer;
  }

  /**
   * Highlights the Levenshtein distance.
   * @param buffer Holds messages.
   * @param distance Levenshtein distance to highlight.
   * @return buffer, for fluency.
   */
  private StringBuilder highlightDistance(
      final StringBuilder buffer,
      final int distance) {
    highlightBracket(buffer, "[");
    buffer.foreground(HighlightUtils.YELLOW).append(distance);
    highlightBracket(buffer, "]");
    return buffer;
  }

  /**
   * Highlights distance method-invocation between two terms.
   * @param buffer Holds messages.
   * @param lhs First term for the distance method.
   * @param rhs Second term for the distance method.
   * @return buffer, for fluency.
   */
  private StringBuilder highlightDistance(
      final StringBuilder buffer,
      final String lhs,
      final String rhs) {
    highlightMethodName(buffer, "d");
    highlighParen(buffer, "(");
    highlightString(buffer, lhs);
    highlightSeparator(buffer, ", ");
    highlightString(buffer, rhs);
    highlighParen(buffer, ")");
    return buffer;
  }

  /**
   * Highlights a tabulator.
   * @param buffer Holds messages.
   * @param tabulator Tabulator to highlight.
   * @return buffer, for fluency.
   */
  private StringBuilder highlightTabulator(
      final StringBuilder buffer,
      final String tabulator) {
    buffer.foreground(HighlightUtils.BLACK).append(tabulator);
    return buffer;
  }

  /**
   * Prints the distance between a query term and spelling candidate, without
   * syntax highlighting.
   * @param buffer Holds messages.
   * @param escapedQuery Java-escaped, query term.
   * @param object Candidate from the dictionary.
   */
  @SuppressWarnings("unchecked")
  private void printCandidate(
      final StringBuilder buffer,
      final String escapedQuery,
      final Object object) {
    buffer.setLength(0);
    final Candidate spellingCandidate = (Candidate) object;
    final String escapedCandidate =
      StringEscapeUtils.escapeJava(spellingCandidate.term());
    buffer.append("| ")
      .append("d(")
        .append('\"')
          .append(escapedQuery)
        .append('\"')
        .append(", ")
        .append('\"')
          .append(escapedCandidate)
        .append('\"')
      .append(')')
      .append(" = ")
      .append('[')
        .append(spellingCandidate.distance())
      .append(']');
    System.out.println(buffer.toString());
  }

  /**
   * Prints the distance between a query term and spelling candidate, with
   * syntax highlighting.
   * @param buffer Holds messages.
   * @param escapedQuery Java-escaped, query term.
   * @param object Candidate from the dictionary.
   */
  @SuppressWarnings("unchecked")
  private void printCandidateInColor(
      final StringBuilder buffer,
      final String escapedQuery,
      final Object object) {
    buffer.setLength(0);
    final Candidate spellingCandidate = (Candidate) object;
    final String escapedCandidate =
      StringEscapeUtils.escapeJava(spellingCandidate.term());
    buffer.mode(HighlightUtils.BOLD);
    highlightTabulator(buffer, "| ");
    highlightDistance(buffer, escapedQuery, escapedCandidate);
    highlightOperator(buffer, " = ");
    highlightDistance(buffer, spellingCandidate.distance());
    buffer.end();
    System.out.println(buffer);
  }

  /**
   * Prints a query term and spelling candidate, without syntax highlighting.
   * @param buffer Holds messages.
   * @param escapedQuery Java-escaped, query term.
   * @param object Spelling candidate from the dictionary.
   */
  @SuppressWarnings("unchecked")
  private void printString(
      final StringBuilder buffer,
      final String escapedQuery,
      final Object object) {
    buffer.setLength(0);
    final String spellingCandidate = (String) object;
    final String escapedCandidate =
      StringEscapeUtils.escapeJava(spellingCandidate);
    buffer.append("| ")
      .append('\"')
        .append(escapedQuery)
      .append('\"')
      .append(" ~ ")
      .append('\"')
        .append(escapedCandidate)
      .append('\"');
    System.out.println(buffer.toString());
  }

  /**
   * Prints a query term and spelling candidate, with syntax highlighting.
   * @param buffer Holds messages.
   * @param escapedQuery Java-escaped, query term.
   * @param object Spelling candidate from the dictionary.
   */
  @SuppressWarnings("unchecked")
  private void printStringInColor(
      final StringBuilder buffer,
      final String escapedQuery,
      final Object object) {
    buffer.setLength(0);
    final String spellingCandidate = (String) object;
    final String escapedCandidate =
      StringEscapeUtils.escapeJava(spellingCandidate);
    buffer.mode(HighlightUtils.BOLD);
    highlightTabulator(buffer, "| ");
    highlightString(buffer, escapedQuery);
    highlightOperator(buffer, " ~ ");
    highlightString(buffer, escapedCandidate);
    buffer.end();
    System.out.println(buffer.toString());
  }

  /**
   * Generates the border for appending above and below a header's text.
   * @param buffer Holds messages.
   * @return buffer, for fluency.
   */
  private StringBuilder headerBorder(final StringBuilder buffer) {
    buffer.append('+');
    for (int i = 0; i < 79; i += 1) {
      buffer.append('-');
    }
    return buffer;
  }

  /**
   * Highlights the border around a header.
   * @param buffer Holds messages.
   * @param border Border to highlight.
   * @return buffer, for fluency.
   */
  private StringBuilder highlightHeaderBorder(
      final StringBuilder buffer,
      final String border) {
    buffer.foreground(HighlightUtils.WHITE).append(border);
    return buffer;
  }

  /**
   * Highlights the border around a header.
   * @param buffer Holds messages.
   * @return buffer, for fluency.
   */
  private StringBuilder highlightHeaderBorder(final StringBuilder buffer) {
    buffer.foreground(HighlightUtils.WHITE);
    headerBorder(buffer);
    return buffer;
  }

  /**
   * Highlights the title of a header.
   * @param buffer Holds messages.
   * @param title Title of the header.
   * @return buffer, for fluency.
   */
  private StringBuilder highlightHeaderTitle(
      final StringBuilder buffer,
      final String title) {
    buffer.foreground(HighlightUtils.CYAN).append(title);
    return buffer;
  }

  /**
   * Highlights a header to make it more readable.
   * @param buffer Holds messages.
   * @param escapedQuery Java-escaped, query term.
   * @return buffer, for fluency.
   */
  private StringBuilder highlightHeader(
      final StringBuilder buffer,
      final String escapedQuery) {
    highlightHeaderBorder(buffer).append('\n');
    highlightHeaderBorder(buffer, "|");
    highlightHeaderTitle(buffer, " Spelling Candidates for Query Term: ");
    highlightString(buffer, escapedQuery).append('\n');
    highlightHeaderBorder(buffer);
    return buffer;
  }

  /**
   * Prints a user-friendly header, that specifies which term is being queried,
   * in color.
   * @param buffer Holds messages.
   * @param escapedQuery Java-escaped, query term.
   */
  private void printHeaderInColor(
      final StringBuilder buffer,
      final String escapedQuery) {
    buffer.setLength(0);
    buffer.mode(HighlightUtils.BOLD);
    highlightHeader(buffer, escapedQuery);
    buffer.end();
    System.out.println(buffer.toString());
  }

  /**
   * Prints a user-friendly header, that specifies which term is being queried.
   * @param buffer Holds messages.
   * @param escapedQuery Java-escaped, query term.
   */
  private void printHeader(
      final StringBuilder buffer,
      final String escapedQuery) {
    buffer.setLength(0);
    headerBorder(buffer).append('\n');
    buffer.append("| Spelling Candidates for Query Term: ")
      .append('\"')
        .append(escapedQuery)
      .append('\"')
      .append('\n');
    headerBorder(buffer);
    System.out.println(buffer.toString());
  }

  /**
   * Queries a dictionary to find all spelling candidates for a sequence of
   * query terms, according to the parameters specified on the command-line.
   */
  @Override
  public void run() {
    try {
      final SortedDawg dictionary = buildDictionary();

      final ITransducer<Object> transducer =
        new TransducerBuilder()
          .algorithm(algorithm())
          .defaultMaxDistance(maxDistance())
          .includeDistance(includeDistance())
          .dictionary(dictionary, true)
          .build();

      final Printer printer = includeDistance()
        ? colorize()
          ? this::printCandidateInColor
          : this::printCandidate
        : colorize()
          ? this::printStringInColor
          : this::printString;

      final BiConsumer<StringBuilder, String> header = colorize()
        ? this::printHeaderInColor
        : this::printHeader;

      final StringBuilder buffer = new StringBuilder(1024);

      for (final String queryTerm : queryTerms()) {
        final String escapedQuery = StringEscapeUtils.escapeJava(queryTerm);
        header.accept(buffer, escapedQuery);
        for (final Object object : transducer.transduce(queryTerm)) {
          printer.print(buffer, escapedQuery, object);
        }
      }

      if (null != serializationPath()) {
        serialize(dictionary);
      }

      exit(EXIT_SUCCESS);
    }
    catch (final Exception exception) {
      log.error("Failed to run application", exception);
      exit(EXIT_ERROR);
    }
  }

  /**
   * Serializes the dictionary to the desired location, as the specified format.
   * @param dictionary Dictionary to serialize.
   * @throws Exception If the dictionary cannot be serialized.
   */
  private void serialize(final SortedDawg dictionary) throws Exception {
    log.info("Serializing [{}] terms in the dictionary to [{}] as format [{}]",
        dictionary.size(),
        serializationPath(),
        targetFormat());

    switch (targetFormat()) {
      case PROTOBUF:
        serialize(dictionary, new ProtobufSerializer());
        break;
      case PLAIN_TEXT:
        serializePlainText(dictionary);
        break;
      case BYTECODE:
        serialize(dictionary, new BytecodeSerializer());
        break;
      default:
        final String message = String.format(
          "Unsupported, serialization format [%s]",
            targetFormat());
        throw new IllegalArgumentException(message);
    }
  }

  /**
   * Serializes a dictionary to the desired location, as the specified format.
   * @param dictionary Dictionary to serialize.
   * @param serializer Serializes the dictionary as the speicified format.
   * @throws Exception If the dictionary cannot be serialized.
   */
  private void serialize(
      final SortedDawg dictionary,
      final Serializer serializer) throws Exception {
    try (final OutputStream stream = Files.newOutputStream(serializationPath())) {
      serializer.serialize(dictionary, stream);
    }
  }

  /**
   * Serializes a dictionary to the desired location, as a plain text file.
   * @param dictionary Dictionary to serialize.
   * @throws Exception If the dictionary cannot be serialized.
   */
  private void serializePlainText(final SortedDawg dictionary) throws Exception {
    try (final BufferedWriter writer = Files.newBufferedWriter(serializationPath())) {
      for (final String term : dictionary) {
        writer.write(term);
        writer.newLine();
      }
    }
  }

  /**
   * Queries a dictionary to find all spelling candidates for a sequence of
   * query terms, according to the parameters specified on the command-line.
   * @param args Arguments that specify how to query the dictionary.
   */
  public static void main(final String... args) {
    try {
      final CommandLineInterface app = new CommandLineInterface(args);
      app.run();
    }
    catch (final Throwable thrown) {
      log.error("Rescued unhandled exception while running application", thrown);
      System.exit(EXIT_UNHANDLED);
    }
  }

  /**
   * Supported serialization types.
   */
  @Getter
  @RequiredArgsConstructor
  enum SerializationFormat {

    /** Google Protocol Buffers. */
    PROTOBUF("application/octet-stream"),

    /** Java bytecode. */
    BYTECODE("application/octet-stream"),

    // [WARNING] :: PLAIN_TEXT should come last as its Serializer will attempt
    // to deserialize any file as plain text ...
    // -------------------------------------------------------------------------

    /** Plain text dictionary (newline-delimited terms). */
    PLAIN_TEXT("text/plain");

    private final String contentType;
  }

  /**
   * Functional interface for methods that print spelling candidates.
   */
  interface Printer {

    /**
     * Prints spelling candidates.
     * @param buffer Holds messages.
     * @param escapedQuery Java-escaped, query term.
     * @param object Spelling candidate.
     */
    void print(StringBuilder buffer, String escapedQuery, Object object);
  }
}
