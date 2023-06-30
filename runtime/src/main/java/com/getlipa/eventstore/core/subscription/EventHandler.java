package com.getlipa.eventstore.core.subscription;

import com.getlipa.eventstore.core.event.Event;
import com.google.protobuf.Message;

public interface EventHandler<T extends Message> {

    void handle(Event<T> subscriptionEvent);
}
