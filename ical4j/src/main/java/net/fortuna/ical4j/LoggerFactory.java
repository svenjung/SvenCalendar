package net.fortuna.ical4j;

import android.util.Log;

import com.sven.ical4j.BuildConfig;

/**
 * Created by Sven.J on 18-1-8.
 */

public class LoggerFactory {

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getSimpleName());
    }

    public static Logger getLogger(String tag) {
        return new DefaultLogger(tag);
    }

    private static class DefaultLogger implements Logger {
        private String tag;

        public DefaultLogger(String tag) {
            this.tag = tag;
        }

        @Override
        public boolean isTraceEnabled() {
            return Log.isLoggable(tag, Log.VERBOSE);
        }

        @Override
        public void trace(String message) {
            Log.i(tag, message);
        }

        @Override
        public void trace(String message, Throwable throwable) {
            Log.i(tag, message, throwable);
        }

        @Override
        public boolean isDebugEnabled() {
            return BuildConfig.DEBUG || Log.isLoggable(tag, Log.DEBUG);
        }

        @Override
        public void debug(String message) {
            Log.d(tag, message);
        }

        @Override
        public void debug(String message, Throwable throwable) {
            Log.d(tag, message, throwable);
        }

        @Override
        public void warn(String message) {
            Log.w(tag, message);
        }

        @Override
        public void warn(String message, Throwable throwable) {
            Log.w(tag, message, throwable);
        }

        @Override
        public void error(String message) {
            Log.e(tag, message);
        }

        @Override
        public void error(String message, Throwable throwable) {
            Log.e(tag, message, throwable);
        }
    }
}
