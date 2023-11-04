package com.getlipa.event.store.it;

import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.example.event.Example;
import com.getlipa.eventstore.core.projection.trgt.eventhandler.Project;
import com.getlipa.eventstore.core.projection.cdi.Events;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

//@Subscription(name = "demo-subscription")
@Events.WithLogId("gugus")
@RequiredArgsConstructor
@Slf4j
public class DemoSubscription {

    @Project
    public void onSupplyAdded(Event<Example.Simple> event) {
        log.error("EPHEMERAL: received event: {}", event);
    }
}



