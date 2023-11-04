package com.getlipa.eventstore.core.stream;

import com.getlipa.eventstore.core.event.selector.Selector;
import com.getlipa.eventstore.core.persistence.EventPersistence;
import com.getlipa.eventstore.core.stream.reader.Direction;
import com.getlipa.eventstore.core.stream.reader.EventReader;
import io.vertx.core.Vertx;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Stream {

    private final Vertx vertx;

    protected final Selector selector;

    protected final EventPersistence eventPersistence;

    public EventReader read(Direction direction) {
        return new EventReader(vertx, selector, eventPersistence, direction.readOptions()
                .direction(direction)
        );
    }

    public EventReader readForward() {
        return read(Direction.FORWARD);
    }

    public EventReader readBackward() {
        return read(Direction.BACKWARD);
    }

}
