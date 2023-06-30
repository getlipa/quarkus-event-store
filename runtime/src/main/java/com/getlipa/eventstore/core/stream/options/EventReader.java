package com.getlipa.eventstore.core.stream.options;

import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.persistence.EventPersistence;
import com.getlipa.eventstore.core.stream.selector.Events;
import com.getlipa.eventstore.core.subscription.EventHandler;
import com.google.protobuf.Message;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Iterator;
import java.util.Optional;


@Setter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class EventReader {

    private final Events.Selector selector;

    private final EventPersistence eventPersistence;

    private final ReadOptions.ReadOptionsBuilder optionsBuilder;


    public EventReader startAfter(Cursor cursor) {
        optionsBuilder.startAt(cursor);
        return this;
    }

    public EventReader limit(int limit) {
        optionsBuilder.limit(limit);
        return this;
    }

    public Optional<Event<Message>> first() {
        final var iterator = toIterator();
        if (!iterator.hasNext()) {
            return Optional.empty();
        }
        return Optional.of(toIterator().next());
    }

    public Iterator<Event<Message>> toIterator() {
        return selector.readFrom(eventPersistence, optionsBuilder.build());
    }

    public void forEach(EventHandler<Message> eventHandler) {
        toIterator().forEachRemaining(eventHandler::handle);
    }
}
