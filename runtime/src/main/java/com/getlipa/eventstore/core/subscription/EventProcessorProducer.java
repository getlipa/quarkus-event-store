package com.getlipa.eventstore.core.subscription;

import com.getlipa.eventstore.core.actor.cdi.ActorScoped;
import com.getlipa.eventstore.core.subscription.cdi.Subscription;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class EventProcessorProducer {

    @ActorScoped
    @Produces
    @Subscription.Type(Subscription.Type.DEFAULT)
    public EventProcessor produceEphemeral(@Subscription.Qualifier Instance<Object> bean) {
        return EventHandlerInvoker.create(bean.get());
    }
}
