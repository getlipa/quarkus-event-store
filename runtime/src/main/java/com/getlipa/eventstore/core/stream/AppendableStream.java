package com.getlipa.eventstore.core.stream;

import com.getlipa.eventstore.core.event.EphemeralEvent;
import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.persistence.EventPersistence;
import com.getlipa.eventstore.core.persistence.exception.EventAppendException;
import com.getlipa.eventstore.core.event.seriesindex.SeriesIndex;
import com.getlipa.eventstore.core.stream.selector.ByStreamSelector;
import com.getlipa.eventstore.core.subscription.EventDispatcher;
import com.google.protobuf.Message;

public class AppendableStream extends Stream {

    private final EventDispatcher eventDispatcher;

    private final ByStreamSelector byStreamSelector;

    public AppendableStream(
            final ByStreamSelector selector,
            final EventPersistence eventPersistence,
            final EventDispatcher eventDispatcher
    ) {
        super(selector, eventPersistence);
        this.eventDispatcher = eventDispatcher;
        this.byStreamSelector = selector;
    }

    public <T extends Message> Event<T> append(SeriesIndex seriesIndex, EphemeralEvent<T> ephemeralEvent) throws EventAppendException {
        final var event = eventPersistence.append(byStreamSelector, seriesIndex, ephemeralEvent);
        eventDispatcher.dispatch(event);
        return event;
    }
}
