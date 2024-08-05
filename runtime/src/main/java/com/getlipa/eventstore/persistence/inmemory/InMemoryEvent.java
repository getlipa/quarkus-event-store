package com.getlipa.eventstore.persistence.inmemory;

import com.getlipa.eventstore.event.EphemeralEvent;
import com.getlipa.eventstore.event.EventMetadata;
import com.getlipa.eventstore.identifier.Id;
import com.google.protobuf.Message;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;

@Builder
@RequiredArgsConstructor
@Getter
public class InMemoryEvent implements EventMetadata {

    private final Id id;

    private final OffsetDateTime createdAt;

    private final Id correlationId;

    private final Id causationId;

    private final long position;

    private final String logContext;

    private final Id logId;

    private final long logIndex;


    public static <T extends Message> InMemoryEvent from(
            long position,
            String logDomain,
            Id logId,
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
