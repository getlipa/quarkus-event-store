package com.getlipa.eventstore.core.subscription;

import com.getlipa.eventstore.core.CdiUtil;
import com.getlipa.eventstore.core.actor.cdi.Actor;
import com.getlipa.eventstore.core.actor.cdi.ActorId;
import com.getlipa.eventstore.core.actor.messaging.Command;
import com.getlipa.eventstore.core.proto.PayloadParser;
import com.getlipa.eventstore.core.subscription.cdi.Subscription;
import com.getlipa.eventstore.subscriptions.Subscriptions;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;

@Actor("subscription-controller")
public class SubscriptionController {

    private final PayloadParser parser;

    private final EventProcessor eventProcessor;

    public SubscriptionController(
            @Any Instance<EventProcessor> eventProcessors,
            @Subscription.Qualifier Instance<Object> subscriptions,
            ActorId actorId,
            PayloadParser parser
    ) {
        this.parser = parser;
        this.eventProcessor = determineEventProcessor(subscriptions, actorId, eventProcessors);
    }

    public void handleEvent(Command<Subscriptions.Event> eventCommand) {
        final var event = EventCodec.wrappedFrom(eventCommand.getPayload().get(), parser);
        eventProcessor.process(event);
    }

    static EventProcessor determineEventProcessor(
            Instance<Object> subscriptions,
            ActorId actorId,
            Instance<EventProcessor> eventProcessors
    ) {
        final var subscriptionBean = subscriptions.select(Subscription.Name.Literal.create(actorId.getName()));
        if (!subscriptionBean.isResolvable()) {
            throw new IllegalStateException("Subscription is not resolvable: " + actorId.getName());
        }
        // TODO: Use @Middleware(SomeMiddleware.class) ?
        final var type = CdiUtil.qualifier(Subscription.Type.class, subscriptionBean)
                .map(Subscription.Type::value)
                .orElse("ephemeral");
        return eventProcessors.select(Subscription.Type.Literal.create(type)).get();
    }

}
