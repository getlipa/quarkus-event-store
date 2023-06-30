package com.getlipa.eventstore.core.persistence.exception;

public class DuplicateEventException extends EventAppendException{

    public DuplicateEventException(Throwable cause) {
        super("Event already exists.", cause);
    }
}
