package com.getlipa.eventstore.event.payload;

import com.getlipa.eventstore.event.AbstractEvent;
import com.google.protobuf.Message;

public abstract class AbstractPayload<T extends Message> implements Payload<T> {

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof AnyPayload)) {
            return false;
        }
        return get().equals(((AnyPayload) other).get());
    }
}
