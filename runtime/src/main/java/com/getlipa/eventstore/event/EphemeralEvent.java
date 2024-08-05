package com.getlipa.eventstore.event;


import com.getlipa.eventstore.event.payload.Payload;
import com.getlipa.eventstore.identifier.Id;
import com.google.protobuf.Message;

import java.time.OffsetDateTime;


public class EphemeralEvent<T extends Message> extends AbstractEvent<T> {

    public EphemeralEvent(
            final Id id,
            final Id causationId,
            final Id correlationId,
            final OffsetDateTime createdAt,
            final T payload
    ) {
        super(id, causationId, correlationId, createdAt, Payload.create(payload));
    }

    public static Builder create() {
        return new Builder()
                .withId(Id.random())
                .withCausationId(Id.random())
                .withCorrelationId(Id.random())
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
