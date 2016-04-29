package com.github.liblevenshtein;

/**
 * Functional interface for methods that print spelling candidates.
 */
public interface Printer {

  /**
   * Prints spelling candidates.
   * @param buffer Holds messages.
   * @param escapedQuery Java-escaped, query term.
   * @param object Spelling candidate.
   */
  void print(StringBuilder buffer, String escapedQuery, Object object);
}
