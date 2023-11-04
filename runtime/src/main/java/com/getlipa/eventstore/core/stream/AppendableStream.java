package com.getlipa.eventstore.core.stream;

import com.getlipa.eventstore.core.event.AnyEvent;
import com.getlipa.eventstore.core.event.EphemeralEvent;
import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.event.logindex.LogIndex;
import com.getlipa.eventstore.core.persistence.EventPersistence;
import com.getlipa.eventstore.core.persistence.exception.DuplicateEventException;
import com.getlipa.eventstore.core.persistence.exception.EventAppendException;
import com.getlipa.eventstore.core.event.selector.ByLogSelector;
import com.getlipa.eventstore.core.subscription.EventAppended;
import com.google.protobuf.Message;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppendableStream extends Stream {

    private final ByLogSelector byLogSelector;

    private final jakarta.enterprise.event.Event<EventAppended> events;

    public AppendableStream(
            final Vertx vertx,
            final ByLogSelector selector,
            final EventPersistence eventPersistence,
            final jakarta.enterprise.event.Event<EventAppended> events
    ) {
        super(vertx, selector, eventPersistence);
        this.byLogSelector = selector;
        this.events = events;
    }

    public <T extends Message> Future<AnyEvent> append(LogIndex logIndex, EphemeralEvent<T> ephemeralEvent) {
        // TODO: only notify subscriptions when event was actually appended (ignore re-appends of the same event)
        return eventPersistence.append(byLogSelector, logIndex, ephemeralEvent)
                .recover(error -> {
                    // FIXME
                    if (error instanceof DuplicateEventException) {
                        return eventPersistence.read(ephemeralEvent.getId());
                    }
                    return Future.failedFuture(error);
                })
                .onSuccess(event -> events.fireAsync(EventAppended.create(event))
                        .exceptionally(throwable -> {
                            log.error(
                                    "Unable to notify {} observers: {}",
                                    EventAppended.class,
                                    throwable.toString()
                            );
                            return null;
                        })
                );
    }
}
