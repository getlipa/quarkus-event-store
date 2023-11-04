package com.getlipa.eventstore.core.persistence.inmemory;

import com.getlipa.eventstore.core.event.AnyEvent;
import com.getlipa.eventstore.core.event.EphemeralEvent;
import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.event.EventMetadata;
import com.getlipa.eventstore.core.event.logindex.LogIndex;
import com.getlipa.eventstore.core.event.selector.ByLogSelector;
import com.getlipa.eventstore.core.event.selector.Selector;
import com.getlipa.eventstore.core.persistence.EventPersistence;
import com.getlipa.eventstore.core.persistence.exception.InvalidIndexException;
import com.getlipa.eventstore.core.stream.reader.ReadOptions;
import com.google.protobuf.Message;
import io.vertx.core.Future;

import java.util.*;

public class InMemoryEventPersistence implements EventPersistence {

    private final List<AnyEvent> events = new LinkedList<>();

    private final Map<ByLogSelector, Integer> indexByLog = new HashMap<>();

    @Override
    public <T extends Message> Future<AnyEvent> append(ByLogSelector selector, LogIndex logIndex, EphemeralEvent<T> event) {
        final var existingEvent = findById(event.getId());
        if (existingEvent != null) {
            return Future.succeededFuture(existingEvent);
        }
        final var index = indexByLog.getOrDefault(selector, -1) + 1;
        try {
            logIndex.validate(index);
        } catch (InvalidIndexException e) {
            return Future.failedFuture(e);
        }
        indexByLog.put(selector, index);
        final var persisted = Event.from(InMemoryEvent.from(
                events.size() + 1,
                selector.getLogDomain(),
                selector.getLogId(),
                index,
                event
        )).withPayload(event.getPayload());
        events.add(persisted);
        return Future.succeededFuture(persisted);
    }

    @Override
    public Future<Iterator<AnyEvent>> read(Selector selector, ReadOptions readOptions) {
        final var result = new LinkedList<AnyEvent>();
        final var queue = new LinkedList<>(events);
        queue.sort(Comparator.comparing(EventMetadata::getPosition, readOptions.direction().getPositionComparator()));
        for (final var event : queue) {
            if (!selector.matches(event) || readOptions.from().isAfter(event, readOptions.direction())) {
                continue;
            }
            if (result.size() >= readOptions.limit() || readOptions.until().isBefore(event, readOptions.direction())) {
                break;
            }
            result.add(event);
        }
        return Future.succeededFuture(result.iterator());
    }

    @Override
    public Future<AnyEvent> read(UUID id) {
        final var event = findById(id);
        if (event != null) {
            return Future.succeededFuture(event);
        }
        return Future.failedFuture("No such event.");
    }

    AnyEvent findById(UUID id) {
        for (final var event : events) {
            if (event.getId().equals(id)) {
                return event;
            }
        }
        return null;
    }
}
