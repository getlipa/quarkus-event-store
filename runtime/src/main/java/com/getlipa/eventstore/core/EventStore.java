package com.getlipa.eventstore.core;

import com.getlipa.eventstore.core.event.selector.Selector;
import com.getlipa.eventstore.core.persistence.EventPersistence;
import com.getlipa.eventstore.core.stream.AppendableStream;
import com.getlipa.eventstore.core.subscription.EventAppended;
import com.getlipa.eventstore.core.stream.Stream;
import com.getlipa.eventstore.core.event.selector.ByLogSelector;
import com.getlipa.eventstore.core.subscription.Subscriber;
import io.vertx.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class EventStore {

    private final Vertx vertx;

    private final EventPersistence eventPersistence;

    private final jakarta.enterprise.event.Event<EventAppended> event;

    public Stream stream(Selector selector) {
        return new Stream(
                vertx,
                selector,
                eventPersistence
        );
    }

    public AppendableStream stream(ByLogSelector selector) {
        return new AppendableStream(
                vertx,
                selector,
                eventPersistence,
                event
        );
    }
}
