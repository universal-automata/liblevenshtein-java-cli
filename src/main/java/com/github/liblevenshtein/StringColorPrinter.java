package com.github.liblevenshtein;

import org.apache.commons.lang3.StringEscapeUtils;

import lombok.experimental.ExtensionMethod;

import com.github.liblevenshtein.util.HighlightUtils;

/**
 * Prints a query term and spelling candidate, with syntax highlighting.
 */
@ExtensionMethod(HighlightUtils.class)
public class StringColorPrinter extends AbstractPrinter {

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("unchecked")
  public void print(
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
}
