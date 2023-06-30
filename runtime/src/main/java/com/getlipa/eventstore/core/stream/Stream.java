package com.getlipa.eventstore.core.stream;

import com.getlipa.eventstore.core.persistence.EventPersistence;
import com.getlipa.eventstore.core.stream.options.Cursor;
import com.getlipa.eventstore.core.stream.options.Direction;
import com.getlipa.eventstore.core.stream.options.EventReader;
import com.getlipa.eventstore.core.stream.options.ReadOptions;
import com.getlipa.eventstore.core.stream.selector.Events;
import com.getlipa.eventstore.core.subscription.EventDispatcher;
import io.vertx.core.Vertx;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Stream {

    protected final Events.Selector selector;

    protected final EventPersistence eventPersistence;

    public EventReader read(Direction direction) {
        return new EventReader(selector, eventPersistence, ReadOptions.builder()
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
