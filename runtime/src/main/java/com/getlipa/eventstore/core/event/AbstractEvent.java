package com.getlipa.eventstore.core.event;

import com.getlipa.eventstore.core.proto.Payload;
import com.google.protobuf.Message;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public abstract class AbstractEvent<T extends Message> {

    @ToString.Include
    protected final UUID id;

    private final UUID causationId;

    private final UUID correlationId;

    private final OffsetDateTime createdAt;

    private final Payload<T> payload;

    public UUID getType() {
        return payload.getTypeId();
    }

    static class Builder<T extends Builder<T>> {

        UUID withId;

        UUID withCausationId;

        UUID withCorrelationId;

        OffsetDateTime withCreatedAt;

        public T withId(final UUID id) {
            withId = id;
            return (T) this;
        }

        public T withCausationId(final UUID causationId) {
            withCausationId = causationId;
            return (T) this;
        }

        public T withCorrelationId(final UUID correlationId) {
            withCorrelationId = correlationId;
            return (T) this;
        }

        public T withCreatedAt(final OffsetDateTime createdAt) {
            withCreatedAt = createdAt;
            return (T) this;
        }
    }
}
