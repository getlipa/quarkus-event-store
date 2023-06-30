package com.getlipa.eventstore.core.stream.selector;

import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.persistence.EventPersistence;
import com.getlipa.eventstore.core.stream.options.ReadOptions;
import com.google.protobuf.Message;

import java.util.Iterator;

public class ByTypeSelector implements Events.Selector {

    @Override
    public Iterator<Event<Message>> readFrom(EventPersistence eventPersistence, ReadOptions readOptions) {
        return eventPersistence.read(this, readOptions);
    }
}
