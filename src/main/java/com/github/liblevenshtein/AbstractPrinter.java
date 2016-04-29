package com.github.liblevenshtein;

import java.util.function.BiConsumer;

import lombok.experimental.ExtensionMethod;

import com.github.liblevenshtein.util.HighlightUtils;

/**
 * Prints strings in color.
 */
@ExtensionMethod({HighlightUtils.class})
public abstract class AbstractPrinter
    implements Printer, BiConsumer<StringBuilder, String> {

  /**
   * Generates the border for appending above and below a header's text.
   * @param buffer Holds messages.
   * @return buffer, for fluency.
   */
  protected StringBuilder headerBorder(final StringBuilder buffer) {
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
  protected StringBuilder highlightHeaderBorder(
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
  protected StringBuilder highlightHeaderBorder(final StringBuilder buffer) {
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
  protected StringBuilder highlightHeaderTitle(
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
  protected StringBuilder highlightHeader(
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
   * Highlights string quotes.
   * @param buffer Holds messages.
   * @param quote String quote to highlight.
   * @return buffer, for fluency.
   */
  protected StringBuilder highlightQuote(
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
  protected StringBuilder highlightString(
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
  protected StringBuilder highlightMethodName(
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
  protected StringBuilder highlighParen(
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
  protected StringBuilder highlightSeparator(
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
  protected StringBuilder highlightOperator(
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
  protected StringBuilder highlightBracket(
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
  protected StringBuilder highlightDistance(
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
  protected StringBuilder highlightDistance(
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
  protected StringBuilder highlightTabulator(
      final StringBuilder buffer,
      final String tabulator) {
    buffer.foreground(HighlightUtils.BLACK).append(tabulator);
    return buffer;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void print(
      final StringBuilder buffer,
      final String escapedQuery,
      final Object object) {
    throw new UnsupportedOperationException("print(StringBuilder,String,Object) is not implemented");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(final StringBuilder buffer, final String escapedQuery) {
    throw new UnsupportedOperationException("accept(StringBuilder,String) is not implemented");
  }
}
