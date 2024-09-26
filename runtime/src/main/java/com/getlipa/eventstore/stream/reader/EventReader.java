package com.getlipa.eventstore.stream.reader;

import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.event.Event;
import com.getlipa.eventstore.event.Events;
import com.getlipa.eventstore.event.selector.Selector;
import com.getlipa.eventstore.identifier.Id;
import com.getlipa.eventstore.stream.reader.cursor.Cursor;
import com.getlipa.eventstore.persistence.EventPersistence;
import com.google.protobuf.Message;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;


@Setter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class EventReader {

    private final Vertx vertx;

    private final Selector selector;

    private final EventPersistence eventPersistence;

    private final ReadOptions.ReadOptionsBuilder optionsBuilder;

    public EventReader from(Cursor cursor) {
        optionsBuilder.from(cursor);
        return this;
    }

    public EventReader until(Cursor cursor) {
        optionsBuilder.until(cursor);
        return this;
    }

    public EventReader limit(int limit) {
        optionsBuilder.limit(limit);
        return this;
    }

    public EventReader pageSize(int pageSize) {
        optionsBuilder.pageSize(pageSize);
        return this;
    }

    public <T> Future<T> aggregate(T initialValue, AnyEvent.Aggregator<T> handler) {
        final var options = optionsBuilder.build();
        final var aggregate = new AtomicReference<>(initialValue);
        return Paginator.create(vertx, eventPersistence, selector, options)
                .forEach(event -> handler.aggregate(aggregate.get(), event)
                        .onSuccess(aggregate::set)
                        .mapEmpty()
                )
                .map(result -> aggregate.get());
    }

    public <T> Future<Long> forEach(AnyEvent.Handler<T> handler) {
        final var options = optionsBuilder.build();
        return Paginator.create(vertx, eventPersistence, selector, options)
                .forEach(handler);
    }

    public <T extends Message> Future<Optional<Event<T>>> first(Class<T> clazz) {
        AtomicReference<Event<T>> result = new AtomicReference<>();

        var future = forEach(event -> { //TODO only read until first hit
            if (result.get() == null && clazz.isInstance(event.getPayload().get())) {
                result.set((Event<T>) event);
            }
            return Future.succeededFuture();
        });

        return future.map(ok -> Optional.ofNullable(result.get()));
    }
}
