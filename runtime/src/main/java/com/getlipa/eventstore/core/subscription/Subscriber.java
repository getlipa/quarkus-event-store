package com.getlipa.eventstore.core.subscription;

import com.getlipa.eventstore.core.event.AnyEvent;

public interface Subscriber {

    void handle(AnyEvent event);
}
