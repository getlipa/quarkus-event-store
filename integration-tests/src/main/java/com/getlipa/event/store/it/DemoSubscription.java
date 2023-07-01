package com.getlipa.event.store.it;

import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.example.event.Example;
import com.getlipa.eventstore.core.subscription.cdi.EventHandler;
import com.getlipa.eventstore.core.subscription.cdi.Stream;
import com.getlipa.eventstore.core.subscription.cdi.Subscription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Subscription("demo")
@Stream.BySeriesId("gugus")
@RequiredArgsConstructor
@Slf4j
public class DemoSubscription {

    @EventHandler
    public void onSupplyAdded(Event<Example.Simple> event) {
        log.error("Received event: {}", event);
    }
}



