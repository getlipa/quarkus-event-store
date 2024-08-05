package com.getlipa.eventstore;

import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.event.selector.Selector;
import com.getlipa.eventstore.persistence.EventPersistence;
import com.getlipa.eventstore.stream.AppendableStream;
import com.getlipa.eventstore.stream.Stream;
import com.getlipa.eventstore.event.selector.ByLogSelector;
import io.vertx.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class EventStore {

    private final Vertx vertx;

    private final EventPersistence eventPersistence;

    private final Event<EventAppended> event;

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

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class EventAppended {

        private final AnyEvent event;

        public static EventAppended create(AnyEvent event) {
            return new EventAppended(event);
        }

    }
}
