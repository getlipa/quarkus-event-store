package com.getlipa.eventstore.event.payload;

import com.google.protobuf.*;

public interface AnyPayload {

    Message get();
}
