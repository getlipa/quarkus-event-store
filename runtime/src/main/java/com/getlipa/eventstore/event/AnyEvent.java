package com.getlipa.eventstore.event;

import com.getlipa.eventstore.event.payload.AnyPayload;
import com.getlipa.eventstore.subscriptions.Projections;
import io.vertx.core.Future;

public interface AnyEvent extends EventMetadata {

    AnyPayload getPayload();

    Projections.Event toProto();

    interface Handler<T> {

        Future<T> handle(AnyEvent event);
    }

    interface Aggregator<T> {

        Future<T> aggregate(T aggregated, AnyEvent event);
    }
}
