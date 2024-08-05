package com.getlipa.eventstore.stream;

import com.getlipa.eventstore.EventStore;
import com.getlipa.eventstore.identifier.Id;
import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.event.EphemeralEvent;
import com.getlipa.eventstore.event.logindex.LogIndex;
import com.getlipa.eventstore.persistence.EventPersistence;
import com.getlipa.eventstore.persistence.exception.DuplicateEventException;
import com.getlipa.eventstore.event.selector.ByLogSelector;
import com.google.protobuf.Message;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import jakarta.enterprise.event.Event;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.function.Function;

@Slf4j
public class AppendableStream extends Stream {

    private final ByLogSelector byLogSelector;

    private final Event<EventStore.EventAppended> events;

    public AppendableStream(
            final Vertx vertx,
            final ByLogSelector selector,
            final EventPersistence eventPersistence,
            final Event<EventStore.EventAppended> events
    ) {
        super(vertx, selector, eventPersistence);
        this.byLogSelector = selector;
        this.events = events;
    }

    public <T extends Message> Future<AnyEvent> append(final LogIndex logIndex, final EphemeralEvent<T> ephemeralEvent) {
        // TODO: only notify subscriptions when event was actually appended (ignore re-appends of the same event)
        return eventPersistence.append(byLogSelector, logIndex, ephemeralEvent)
                .recover(error -> {
                    // FIXME
                    if (error instanceof DuplicateEventException) {
                        return eventPersistence.read(ephemeralEvent.getId());
                    }
                    return Future.failedFuture(error);
                })
                .onSuccess(event -> events.fireAsync(EventStore.EventAppended.create(event))
                        .exceptionally(throwable -> {
                            log.error(
                                    "Unable to notify {} observers: {}",
                                    EventStore.EventAppended.class,
                                    throwable.toString()
                            );
                            return null;
                        })
                );
    }

    public <T extends Message> Appender append(Future<LogIndex> logIndex) {
        return new Appender(this, logIndex, EphemeralEvent.create());
    }

    public <T extends Message> Appender append() {
        return append(LogIndex.atAny());
    }

    public <T extends Message> Appender append(LogIndex logIndex) {
        return append(Future.succeededFuture(logIndex));
    }

    @Accessors(fluent = true)
    @RequiredArgsConstructor
    public static class Appender {

        private final AppendableStream appendableStream;

        private final Future<LogIndex> logIndex;

        private final EphemeralEvent.Builder eventBuilder;

        // FIXME
        @Setter
        private Function<AnyEvent, Future<Void>> onSuccessful = event -> Future.succeededFuture();

        public <T extends Message> Future<AnyEvent> withPayload(T payload) {
            final var ephemeralEvent = eventBuilder.withPayload(payload);
            return logIndex.flatMap(logIndex -> appendableStream.append(logIndex, ephemeralEvent)
                    .flatMap(event -> onSuccessful.apply(event).map(vd -> event)));
            //return initialized.flatMap(vd -> appendableStream.append(logIndex, eventBuilder.withPayload(payload)));
        }

        public Appender withId(Id id) {
            eventBuilder.withId(id);
            return this;
        }

        public Appender withCorrelationId(Id correlationId) {
            eventBuilder.withCorrelationId(correlationId);
            return this;
        }

        public Appender withCreatedAt(OffsetDateTime createdAt) {
            eventBuilder.withCreatedAt(createdAt);
            return this;
        }

        public Appender withCausationId(Id id) {
            eventBuilder.withCausationId(id);
            return this;
        }
    }
}
