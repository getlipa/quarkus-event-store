package com.getlipa.eventstore.core.event;

import com.getlipa.eventstore.subscriptions.Projections;
import com.google.protobuf.Message;
import com.getlipa.eventstore.core.proto.AnyPayload;
import io.vertx.core.Future;

public interface AnyEvent extends EventMetadata {

    AnyPayload getPayload();

    Projections.Event toProto();

    <T extends Message> AnyEvent on(Class<T> type, Event.Handler<T> handler);

    interface Handler<T> {

        Future<T> handle(AnyEvent event);
    }

    interface Aggregator<T> {

        Future<T> aggregate(T aggregated, AnyEvent event);
    }
}
