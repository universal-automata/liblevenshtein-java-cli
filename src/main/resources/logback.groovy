// =============================================================================
// [ONTE] :: This must be at the root of the Jar for Logback to pick it up
// =============================================================================

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.filter.LevelFilter
import ch.qos.logback.core.ConsoleAppender
import static ch.qos.logback.classic.Level.DEBUG
import static ch.qos.logback.classic.Level.ERROR
import static ch.qos.logback.classic.Level.INFO
import static ch.qos.logback.classic.Level.OFF
import static ch.qos.logback.classic.Level.TRACE
import static ch.qos.logback.classic.Level.WARN
import static ch.qos.logback.core.spi.FilterReply.ACCEPT
import static ch.qos.logback.core.spi.FilterReply.DENY

// [NOTE] :: You can disable all logging by exporting the variable:
// =============================================================================
//
//   export LIBLEVENSHTEIN_JAVA_CLI_OPTS="-DLOGGING=false"
//
// =============================================================================
if (System.properties['LOGGING'] ==~ /(?i)^(?:false|off|no|none|disabled?)$/) {
  root(OFF)
}
else {

  def appenders = []

  // [NOTE] :: You can disable error logging by exporting the variable:
  // ===========================================================================
  //
  //   export LIBLEVENSHTEIN_JAVA_CLI_OPTS="-DLOGGING.$LEVEL=false"
  //
  // ===========================================================================
  // ... where $LEVEL is one of TRACE|DEBUG|INFO|WARN|ERROR
  [TRACE, DEBUG, INFO, WARN, ERROR].each { logLevel ->
    if (!(System.properties["LOGGING.${logLevel}"] ==~ /(?i)^(?:false|off|no|none|disabled?)$/)) {
      def appenderName = "LOGGING.${logLevel}"

      appender(appenderName, ConsoleAppender) {
        filter(LevelFilter) {
          level = logLevel
          onMatch = ACCEPT
          onMismatch = DENY
        }
        encoder(PatternLayoutEncoder) {
          // Colorized, console output !!!
          pattern = '%d{HH:mm:ss.SSS} [%thread] %highlight(%-5level) %cyan(%logger{36}) - %msg%n'
        }

        if (logLevel.isGreaterOrEqual(WARN)) {
          target = 'System.err'
        }
      }

      appenders << appenderName
    }
  }

  if (!appenders.empty) {

    // [NOTE] :: You can specify the logging level by exporting the variable:
    // =========================================================================
    //
    //   export LIBLEVENSHTEIN_JAVA_CLI_OPTS="-DLOGGING.LEVEL=$LEVEL"
    //
    // =========================================================================
    // ... where $LEVEL is one of TRACE|DEBUG|INFO|WARN|ERROR|OFF
    switch (System.properties['LOGGING.LEVEL']) {
      case 'TRACE':
        root(TRACE, appenders)
        break
      case 'DEBUG':
        root(DEBUG, appenders)
        break;
      case 'INFO':
        root(INFO, appenders)
        break;
      case 'WARN':
        root(WARN, appenders)
        break;
      case 'ERROR':
        root(ERROR, appenders)
        break;
      case 'OFF':
        root(OFF, appenders)
        break;
      default:
        root(DEBUG, appenders)
        break
    }
  }
  else {
    root(OFF)
  }
}
