package com.getlipa.eventstore.core.persistence.exception;

import com.getlipa.eventstore.core.event.logindex.LogIndex;
import lombok.Getter;

@Getter
public class InvalidIndexException extends EventAppendException {

    protected InvalidIndexException(String message) {
        super(message);
    }

    public static InvalidIndexException indexAlreadyUsed(LogIndex index) {
        return new InvalidIndexException(String.format("Index is already in use: %s", index));
    }

    public static InvalidIndexException nonConsecutiveIndex(LogIndex index) {
        return new InvalidIndexException(String.format("Non-consecutive index: %s", index));
    }
}
