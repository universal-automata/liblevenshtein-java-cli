package com.github.liblevenshtein;

/**
 * Prints a user-friendly header, that specifies which term is being queried.
 */
public class HeaderPrinter extends AbstractPrinter {

  /**
   * Prints a user-friendly header, that specifies which term is being queried.
   * @param buffer Holds messages.
   * @param escapedQuery Java-escaped, query term.
   */
  @Override
  public void accept(final StringBuilder buffer, final String escapedQuery) {
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
}
