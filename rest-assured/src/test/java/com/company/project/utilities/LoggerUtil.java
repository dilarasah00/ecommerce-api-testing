package com.company.project.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerUtil {

    private static final Logger logger = LoggerFactory.getLogger(LoggerUtil.class);

    private LoggerUtil() {
        // Utility class - prevent instantiation
    }

    public static void info(String message) {
        logger.info(message);
    }

    public static void info(String message, Object... args) {
        logger.info(message, args);
    }

    public static void debug(String message) {
        logger.debug(message);
    }

    public static void debug(String message, Object... args) {
        logger.debug(message, args);
    }

    public static void warn(String message) {
        logger.warn(message);
    }

    public static void warn(String message, Throwable throwable) {
        logger.warn(message, throwable);
    }

    public static void error(String message) {
        logger.error(message);
    }

    public static void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    public static void error(String message, Object... args) {
        logger.error(message, args);
    }

    public static void trace(String message) {
        logger.trace(message);
    }

    public static void trace(String message, Object... args) {
        logger.trace(message, args);
    }

    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
}
