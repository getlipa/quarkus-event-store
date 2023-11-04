package com.getlipa.eventstore.core.persistence.inmemory;

import com.getlipa.eventstore.core.event.EphemeralEvent;
import com.getlipa.eventstore.core.event.EventMetadata;
import com.google.protobuf.Message;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Builder
@RequiredArgsConstructor
@Getter
public class InMemoryEvent implements EventMetadata {

    private final UUID id;

    private final OffsetDateTime createdAt;

    private final UUID correlationId;

    private final UUID causationId;

    private final long position;

    private final String logDomain;

    private final UUID logId;

    private final long logIndex;

    public static <T extends Message> InMemoryEvent from(
            long position,
            String logDomain,
            UUID logId,
            long logIndex,
            EphemeralEvent<T> event
    ) {
        return new InMemoryEvent(
                event.getId(),
                event.getCreatedAt(),
                event.getCorrelationId(),
                event.getCausationId(),
                position,
                logDomain,
                logId,
                logIndex
        );
    }

}
