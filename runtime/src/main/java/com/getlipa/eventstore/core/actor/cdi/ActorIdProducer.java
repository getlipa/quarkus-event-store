package com.getlipa.eventstore.core.actor.cdi;

import jakarta.enterprise.inject.Produces;

public class ActorIdProducer {

    @Produces
    @ActorScoped
    public ActorId produce() {
        return ActorScopeContext.current().getActorId();
    }
}
