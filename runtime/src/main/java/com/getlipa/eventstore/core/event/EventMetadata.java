package com.getlipa.eventstore.core.event;


import java.time.OffsetDateTime;
import java.util.UUID;

public interface EventMetadata {

     UUID getId();

     OffsetDateTime getCreatedAt();

     UUID getCorrelationId();

     UUID getCausationId();

     long getPosition();

     String getLogDomain();

     UUID getLogId();

     long getLogIndex();

}
