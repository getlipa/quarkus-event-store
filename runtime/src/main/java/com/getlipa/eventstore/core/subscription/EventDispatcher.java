package com.getlipa.eventstore.core.subscription;

import com.getlipa.eventstore.core.CdiUtil;
import com.getlipa.eventstore.core.actor.Gateway;
import com.getlipa.eventstore.core.actor.messaging.Msg;
import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.subscription.cdi.EffectiveStream;
import com.getlipa.eventstore.core.subscription.cdi.Stream;
import com.getlipa.eventstore.core.subscription.cdi.Subscription;
import com.google.protobuf.Message;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;


@ApplicationScoped
@Slf4j
public class EventDispatcher {

    private final Instance<Object> beans;

    private final Gateway<SubscriptionController> subscriberGateway;

    public EventDispatcher(
            @Subscription.Qualifier Instance<Object> beans,
            Gateway<SubscriptionController> subscriberGateway
    ) {
        this.beans = beans;
        this.subscriberGateway = subscriberGateway;
    }

    public <T extends Message> void dispatch(Event<T> event) {
        final var subscribers = new HashSet<Instance.Handle<?>>();
        beans.select(EffectiveStream.BySeriesId.Literal.create(event.getSeriesId().toString())).handles().forEach(subscribers::add);
        beans.select(EffectiveStream.BySeriesType.Literal.create(event.getSeriesType().toString())).handles().forEach(subscribers::add);
        beans.select(EffectiveStream.ByType.Literal.create(event.getType().toString())).handles().forEach(subscribers::add);
        beans.select(EffectiveStream.ByCorrelationId.Literal.create(event.getCorrelationId().toString())).handles().forEach(subscribers::add);
        beans.select(Stream.All.Literal.create()).handles().forEach(subscribers::add);

        subscribers.forEach(instanceHandle -> {
            final var actorId = CdiUtil.qualifier(Subscription.Name.class, instanceHandle)
                    .map(Subscription.Name::value)
                    .orElseThrow(() -> new IllegalStateException("Subscription does not specify @Subscription.Name: " + instanceHandle));
            log.trace("Dispatching {} to subscribers: {}", event, actorId);
            subscriberGateway.run(actorId, subscriptionController -> subscriptionController.handleEvent(
                    Msg.withPayload(event))
            );
        });
    }
}
