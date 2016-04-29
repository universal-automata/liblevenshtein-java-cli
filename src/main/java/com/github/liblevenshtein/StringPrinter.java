package com.github.liblevenshtein;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Prints a query term and spelling candidate, without syntax highlighting.
 */
public class StringPrinter extends AbstractPrinter {

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
}
