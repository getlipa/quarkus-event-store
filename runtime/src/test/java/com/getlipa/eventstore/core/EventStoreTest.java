package com.getlipa.eventstore.core;

import com.getlipa.eventstore.core.stream.selector.Events;
import com.getlipa.eventstore.example.event.Example;

import java.util.UUID;

class EventStoreTest {

    private EventStore eventStore;

    public void forEach() {
        eventStore.stream(Events.bySeries(UUID.randomUUID(), UUID.randomUUID()))
                .readForward()
                .forEach(event -> {
                    event.on(Example.Simple.class, simple -> {

                    });
                });
    }
}