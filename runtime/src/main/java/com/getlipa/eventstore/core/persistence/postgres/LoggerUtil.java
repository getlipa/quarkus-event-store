package com.getlipa.eventstore.core.persistence.postgres;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class LoggerUtil {

    public static InitialState mute(String ...loggerNames) {
        final var initialLevels = Arrays.stream(loggerNames)
                .map(Logger::getLogger)
                .collect(Collectors.toMap(
                        logger -> logger,
                        logger -> {
                            final Level initialLevel = determineLevel(logger);
                            logger.setLevel(Level.OFF);
                            return initialLevel;
                        }));
        return new InitialState(initialLevels);
    }

    static Level determineLevel(Logger logger) {
        while (logger.getLevel() == null) {
            logger = logger.getParent();
            if (logger == null) {
                return Level.INFO;
            }
        }
        return logger.getLevel();
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class InitialState {

        private final Map<Logger, Level> loggers;

        public void restore() {
            loggers.forEach((logger, initialLevel) -> {
                if (initialLevel == null) {
                    throw new IllegalStateException("Cannot restore initial log level of logger: " + logger.getName());
                }
                logger.setLevel(initialLevel);
            });
        }
    }
}
