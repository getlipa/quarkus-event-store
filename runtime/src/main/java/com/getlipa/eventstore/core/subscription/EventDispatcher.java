package com.getlipa.eventstore.core.subscription;

import com.getlipa.eventstore.core.event.Event;
import com.google.protobuf.Message;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class EventDispatcher {

    public <T extends Message> void dispatch(Event<T> event) {
       // TODO
    }
}
