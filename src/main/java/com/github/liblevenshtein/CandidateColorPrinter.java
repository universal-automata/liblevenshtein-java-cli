package com.github.liblevenshtein;

import org.apache.commons.lang3.StringEscapeUtils;

import lombok.experimental.ExtensionMethod;

import com.github.liblevenshtein.transducer.Candidate;

import com.github.liblevenshtein.util.HighlightUtils;

/**
 * Prints the distance between a query term and spelling candidate, with
 * syntax highlighting.
 */
@ExtensionMethod(HighlightUtils.class)
public class CandidateColorPrinter extends AbstractPrinter {

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
    buffer.mode(HighlightUtils.BOLD);
    highlightTabulator(buffer, "| ");
    highlightDistance(buffer, escapedQuery, escapedCandidate);
    highlightOperator(buffer, " = ");
    highlightDistance(buffer, spellingCandidate.distance());
    buffer.end();
    System.out.println(buffer);
  }
}
