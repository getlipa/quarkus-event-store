package com.getlipa.eventstore.core.event;


import com.getlipa.eventstore.core.proto.Payload;
import com.getlipa.eventstore.example.event.Example;
import com.getlipa.eventstore.subscriptions.Projections;
import com.google.protobuf.AbstractMessage;
import com.google.protobuf.Message;

import java.time.OffsetDateTime;
import java.util.UUID;


public class EphemeralEvent<T extends Message> extends AbstractEvent<T> {

    public EphemeralEvent(
            final UUID id,
            final UUID causationId,
            final UUID correlationId,
            final OffsetDateTime createdAt,
            final T payload
    ) {
        super(id, causationId, correlationId, createdAt, Payload.create(payload));
    }

    public static Builder create() {
        return new Builder()
                .withId(UUID.randomUUID())
                .withCausationId(UUID.randomUUID())
                .withCorrelationId(UUID.randomUUID())
                .withCreatedAt(OffsetDateTime.now());
    }

    public static class Builder extends AbstractEvent.Builder<Builder> {

        public <P extends Message> EphemeralEvent<P> withPayload(final P payload) {
            return new EphemeralEvent<>(
                    withId,
                    withCausationId,
                    withCorrelationId,
                    withCreatedAt,
                    payload
            );
        }
    }
}
