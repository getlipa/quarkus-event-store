package com.getlipa.eventstore.stream;

import com.getlipa.eventstore.event.selector.Selector;
import com.getlipa.eventstore.stream.reader.Direction;
import com.getlipa.eventstore.stream.reader.EventReader;
import com.getlipa.eventstore.stream.reader.ReadOptions;
import com.getlipa.eventstore.persistence.EventPersistence;
import io.vertx.core.Vertx;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Stream {

    private final Vertx vertx;

    protected final Selector selector;

    protected final EventPersistence eventPersistence;

    public EventReader read(final ReadOptions.ReadOptionsBuilder readOptionsBuilder) {
        return new EventReader(vertx, selector, eventPersistence, readOptionsBuilder);
    }

    public EventReader read(Direction direction) {
        return read(direction.readOptions());
    }

    public EventReader read() {
        return read(Direction.FORWARD);
    }

    public EventReader readForward() {
        return read();
    }

    public EventReader readBackward() {
        return read(Direction.BACKWARD);
    }

    @Override
    public String toString() {
        return String.format("<%s>", selector.toString());
    }
}
