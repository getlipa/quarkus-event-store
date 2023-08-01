package com.getlipa.eventstore.core.actor.messaging;


import com.getlipa.eventstore.core.proto.AnyPayload;

import java.util.UUID;

public interface AnyMsg  {

    UUID getId();

    UUID getCorrelationId();

    UUID getCausationId();

    AnyPayload getPayload();

}
