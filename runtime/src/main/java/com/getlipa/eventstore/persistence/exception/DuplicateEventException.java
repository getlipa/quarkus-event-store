package com.getlipa.eventstore.persistence.exception;

public class DuplicateEventException extends EventAppendException{

    public DuplicateEventException(Throwable cause) {
        super("Event already exists.", cause);
    }
}
