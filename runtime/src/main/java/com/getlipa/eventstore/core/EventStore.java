package com.getlipa.eventstore.core;

import com.getlipa.eventstore.core.persistence.EventPersistence;
import com.getlipa.eventstore.core.stream.AppendableStream;
import com.getlipa.eventstore.core.stream.Stream;
import com.getlipa.eventstore.core.stream.selector.ByStreamSelector;
import com.getlipa.eventstore.core.stream.selector.Events;
import com.getlipa.eventstore.core.subscription.EventDispatcher;
import io.vertx.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;


@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class EventStore {

    private final EventPersistence eventPersistence;

    private final EventDispatcher eventDispatcher;

    public Stream byCorrelationId(UUID correlationId) {
        return stream(Events.byCorrelationId(correlationId));
    }


    // TODO: naming - impact on seriesId?
    public Stream byDomain(String domain) {
        return bySeriesType(domain);
    }

    public Stream bySeriesType(String seriesType) {
        return stream(Events.bySeriesType(seriesType));
    }

    public Stream stream(Events.Selector selector) {
        return new Stream(
                selector,
                eventPersistence
        );
    }

    public AppendableStream stream(ByStreamSelector selector) {
        return new AppendableStream(
                selector,
                eventPersistence,
                eventDispatcher
        );
    }
}
