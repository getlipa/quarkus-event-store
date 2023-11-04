package com.getlipa.eventstore.core.event.logindex;

import com.getlipa.eventstore.core.event.EventMetadata;
import com.getlipa.eventstore.core.persistence.exception.InvalidIndexException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public abstract class LogIndex {

    public static LogIndex first() {
        return at(0);
    }

    public static LogIndex after(EventMetadata eventMetadata) {
        if (eventMetadata == null) {
            return first();
        }
        return after(eventMetadata.getLogIndex());
    }

    public static LogIndex after(long index) {
        return at(index + 1);
    }

    public static LogIndex at(long index) {
        return new SpecificIndex(index);
    }

    public static LogIndex atAny() {
        return AnyIndex.INSTANCE;
    }

    public static LogIndex afterAny() {
        return AfterAnyIndex.INSTANCE;
    }

    public static LogIndex from(EventMetadata metadata) {
        if (metadata == null) {
            return first();
        }
        return after(metadata.getLogIndex());
    }

    public abstract void validate(long index) throws InvalidIndexException;

    public abstract long getValue();
}
