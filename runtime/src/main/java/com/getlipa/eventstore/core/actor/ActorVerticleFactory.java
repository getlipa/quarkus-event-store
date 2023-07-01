package com.getlipa.eventstore.core.actor;

import com.getlipa.eventstore.core.actor.cdi.Actor;
import com.getlipa.eventstore.core.actor.cdi.ActorId;
import com.getlipa.eventstore.core.actor.cdi.ActorScopeContext;
import com.getlipa.eventstore.core.actor.messaging.CommandMessageHandler;
import io.quarkus.runtime.StartupEvent;
import io.vertx.core.Promise;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.spi.VerticleFactory;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

@Slf4j
@RequiredArgsConstructor
public class ActorVerticleFactory implements VerticleFactory {

    private static final String PREFIX = "actor";

    @Inject
    @Actor.Qualifier
    Instance<Object> actors;

    public Verticle create(final ActorId actorId) {
        return ActorScopeContext.get(actorId).compute(scope -> {
            final var actorInstance = actors.select(Actor.Type.Literal.create(actorId.getType()));
            if (!actorInstance.isResolvable()) {
                throw new IllegalStateException("not resolvable actor: " + actorId);
            }
            return ActorVerticle.createFor(CommandMessageHandler.create(actorInstance.get()), scope);
        });
    }

    @Override
    public String prefix() {
        return PREFIX;
    }

    @Override
    public void createVerticle(String verticleName, ClassLoader classLoader, Promise<Callable<Verticle>> promise) {
        promise.complete(() -> {
            final var actorId = VerticleFactory.removePrefix(verticleName);
            log.trace("Initializing actor verticle: {}", actorId);
            return create(ActorId.create(actorId));
        });
    }

    void onStart(@Observes StartupEvent event, Vertx vertx) {
        vertx.registerVerticleFactory(this);
        log.debug("VerticleFactory registered for prefix: {}", PREFIX);
    }
}
