package com.getlipa.eventstore.core.stream.selector;

import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.persistence.EventPersistence;
import com.getlipa.eventstore.core.stream.options.ReadOptions;
import com.google.protobuf.Message;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Iterator;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class BySeriesTypeSelector implements Events.Selector {

    private final String seriesType;

    @Override
    public Iterator<Event<Message>> readFrom(EventPersistence eventPersistence, ReadOptions readOptions) {
        return eventPersistence.read(this, readOptions);
    }
}
