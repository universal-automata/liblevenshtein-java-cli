package com.github.liblevenshtein;

import org.apache.commons.lang3.StringEscapeUtils;

import com.github.liblevenshtein.transducer.Candidate;

/**
 * Prints the distance between a query term and spelling candidate, without
 * syntax highlighting.
 */
public class CandidatePrinter extends AbstractPrinter {

  /**
   * {@inheritDoc}
   */
  @Override
  public void print(
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
}
