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

    public T payload() {
        return payload.get();
    }

    public <P extends T> P payload(Class<P> type) {
        return payload.get(type);
    }

    public UUID getType() {
        return payload.getTypeId();
    }
}
