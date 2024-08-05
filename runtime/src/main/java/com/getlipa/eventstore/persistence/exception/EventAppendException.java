package com.getlipa.eventstore.persistence.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class EventAppendException extends Exception {

    protected EventAppendException(String message) {
        super(message);
    }

    protected EventAppendException(String message, Throwable cause) {
        super(message, cause);
    }

    public static DuplicateEventException duplicateEvent(Throwable violationException) {
        return new DuplicateEventException(violationException);
    }

    public static EventAppendException because(String message) {
        return new EventAppendException(message);
    }

    public static EventAppendException from(Throwable e) {
        return new EventAppendException(
                String.format(
                        "An exception occurred (%s - %s)",
                        e.getClass().getSimpleName(),
                        e.getMessage()
                ),
                e
        );
    }

    public static EventAppendException from(InterruptedException e) {
        return new EventAppendException("Thread was interrupted.", e);
    }
}
