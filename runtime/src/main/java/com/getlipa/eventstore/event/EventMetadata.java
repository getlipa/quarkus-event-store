package com.getlipa.eventstore.event;


import com.getlipa.eventstore.identifier.Id;

import java.time.OffsetDateTime;

public interface EventMetadata {

     Id getId();

     OffsetDateTime getCreatedAt();

     Id getCorrelationId();

     Id getCausationId();

     long getPosition();

     String getLogContext();

     Id getLogId();

     long getLogIndex();

}
