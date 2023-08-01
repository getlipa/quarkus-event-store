package com.getlipa.eventstore.core.event;


import com.getlipa.eventstore.core.proto.Payload;
import com.getlipa.eventstore.subscriptions.Subscriptions;
import com.google.protobuf.Message;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;

public class EphemeralEvent<T extends Message> extends AbstractEvent<T> {

    @lombok.Builder(setterPrefix = "with", builderMethodName = "create", buildMethodName = "withPayload")
    public EphemeralEvent(
            UUID id,
            UUID causationId,
            UUID correlationId,
            OffsetDateTime createdAt,
            T payload
    ) {
        super(id, causationId, correlationId, createdAt, Payload.create(payload));
    }

    @Override
    protected Subscriptions.Event encodeToProto() {
        return null; // FIXME
    }

    public static class EphemeralEventBuilder<T extends Message> {
        public <P extends Message> EphemeralEvent<P> withPayload(P payload) {
            return new EphemeralEvent<>(
                    id,
                    causationId,
                    correlationId,
                    createdAt,
                    payload
            );
        }
    }
}
