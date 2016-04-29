package com.github.liblevenshtein;

import lombok.experimental.ExtensionMethod;

import com.github.liblevenshtein.util.HighlightUtils;

/**
 * Prints a user-friendly header, that specifies which term is being queried,
 * in color.
 */
@ExtensionMethod({HighlightUtils.class})
public class HeaderColorPrinter extends AbstractPrinter {

  /**
   * Prints a user-friendly header, that specifies which term is being queried,
   * in color.
   * @param buffer Holds messages.
   * @param escapedQuery Java-escaped, query term.
   */
  @Override
  public void accept(final StringBuilder buffer, final String escapedQuery) {
    buffer.setLength(0);
    buffer.mode(HighlightUtils.BOLD);
    highlightHeader(buffer, escapedQuery);
    buffer.end();
    System.out.println(buffer.toString());
  }
}
