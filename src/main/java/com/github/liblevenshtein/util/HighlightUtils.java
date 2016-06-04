package com.github.liblevenshtein.util;

/**
 * Utilities for syntax-highlighting command-line messages.
 */
@SuppressWarnings("checkstyle:avoidescapedunicodecharacters")
public class HighlightUtils {

  /** Unformatted text. */
  public static final int NORMAL = 0;

  /** Emboldens text. */
  public static final int BOLD = 1;

  /** Dims the text colors. */
  public static final int DIM = 2;

  /** Enables text italicization. */
  public static final int ITALIC_ON = 3;

  /** Disables text italicization. */
  public static final int ITALIC_OFF = 23;

  /** Enables text underlining. */
  public static final int UNDERLINE_ON = 4;

  /** Disables text underlining. */
  public static final int UNDERLINE_OFF = 24;

  /** Enables blinking text. */
  public static final int BLINKING_ON = 5;

  /** Disables blinking text. */
  public static final int BLINKING_OFF = 25;

  /** Enables color reversal: foreground becomes background and vice-versa. */
  public static final int REVERSE_VIDEO_ON = 7;

  /** Disables color reversal. */
  public static final int REVERSE_VIDEO_OFF = 27;

  /** Black text color. */
  public static final int BLACK = 0;

  /** Red text color. */
  public static final int RED = 1;

  /** Green text color. */
  public static final int GREEN = 2;

  /** Yellow text color. */
  public static final int YELLOW = 3;

  /** Blue text color. */
  public static final int BLUE = 4;

  /** Magenta text color. */
  public static final int MAGENTA = 5;

  /** Cyan text color. */
  public static final int CYAN = 6;

  /** White text color. */
  public static final int WHITE = 7;

  /**
   * Default constructor for inheritance.
   */
  protected HighlightUtils() {
    // Empty Constructor
  }

  /**
   * Sets the text formatting mode. The mode may be one of the following:
   * <ul>
   *   <li>{@link #NORMAL}</li>
   *   <li>{@link #BOLD}</li>
   *   <li>{@link #DIM}</li>
   *   <li>{@link #ITALIC_ON}</li>
   *   <li>{@link #ITALIC_OFF}</li>
   *   <li>{@link #UNDERLINE_ON}</li>
   *   <li>{@link #UNDERLINE_OFF}</li>
   *   <li>{@link #BLINKING_ON}</li>
   *   <li>{@link #BLINKING_OFF}</li>
   *   <li>{@link #REVERSE_VIDEO_ON}</li>
   *   <li>{@link #REVERSE_VIDEO_OFF}</li>
   * </ul>
   * @param buffer Holds messages.
   * @param mode Text formatting mode.
   * @return buffer, for fluency.
   */
  public static StringBuilder mode(
      final StringBuilder buffer,
      final int mode) {
    buffer.append("\u001B[").append(mode).append('m');
    return buffer;
  }

  /**
   * Sets the background color. The color may be one of the following:
   * <ul>
   *   <li>{@link #BLACK}</li>
   *   <li>{@link #RED}</li>
   *   <li>{@link #GREEN}</li>
   *   <li>{@link #YELLOW}</li>
   *   <li>{@link #BLUE}</li>
   *   <li>{@link #MAGENTA}</li>
   *   <li>{@link #CYAN}</li>
   *   <li>{@link #WHITE}</li>
   * </ul>
   * @param buffer Holds messages.
   * @param color Background color.
   * @return buffer, for fluency.
   */
  public static StringBuilder background(
      final StringBuilder buffer,
      final int color) {
    buffer.append("\u001B[4").append(color).append('m');
    return buffer;
  }

  /**
   * Sets the foreground color. The color may be one of the following:
   * <ul>
   *   <li>{@link #BLACK}</li>
   *   <li>{@link #RED}</li>
   *   <li>{@link #GREEN}</li>
   *   <li>{@link #YELLOW}</li>
   *   <li>{@link #BLUE}</li>
   *   <li>{@link #MAGENTA}</li>
   *   <li>{@link #CYAN}</li>
   *   <li>{@link #WHITE}</li>
   * </ul>
   * @param buffer Holds messages.
   * @param color Foreground color.
   * @return buffer, for fluency.
   */
  public static StringBuilder foreground(
      final StringBuilder buffer,
      final int color) {
    buffer.append("\u001B[3").append(color).append('m');
    return buffer;
  }

  /**
   * Disables syntax highlighting.
   * @param buffer Holds messages.
   * @return buffer, for fluency.
   */
  public static StringBuilder end(final StringBuilder buffer) {
    buffer.append("\u001B[0m");
    return buffer;
  }
}
