package com.getlipa.eventstore.event;

import com.getlipa.eventstore.event.payload.Payload;
import com.getlipa.eventstore.identifier.Id;
import com.google.protobuf.Message;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public abstract class AbstractEvent<T extends Message> {

    protected final Id id;

    private final Id causationId;

    private final Id correlationId;

    private final OffsetDateTime createdAt;

    private final Payload<T> payload;

    static class Builder<T extends Builder<T>> {

        Id withId;

        Id withCausationId;

        Id withCorrelationId;

        OffsetDateTime withCreatedAt;

        public T withId(Id id) {
            withId = id;
            return (T) this;
        }

        public T withCausationId(final Id causationId) {
            withCausationId = causationId;
            return (T) this;
        }

        public T withCorrelationId(final Id correlationId) {
            withCorrelationId = correlationId;
            return (T) this;
        }

        public T withCreatedAt(final OffsetDateTime createdAt) {
            withCreatedAt = createdAt;
            return (T) this;
        }
    }
}
