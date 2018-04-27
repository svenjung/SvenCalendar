package net.fortuna.ical4j;

/**
 * Created by Sven.J on 18-1-8.
 */

public interface Logger {

    /**
     * Is the logger instance enabled for the TRACE level?
     */
    boolean isTraceEnabled();

    void trace(String message);

    void trace(String message, Throwable throwable);

    boolean isDebugEnabled();

    void debug(String message);

    void debug(String message, Throwable throwable);

    void warn(String message);

    void warn(String message, Throwable throwable);

    void error(String message);

    void error(String message, Throwable throwable);
}
