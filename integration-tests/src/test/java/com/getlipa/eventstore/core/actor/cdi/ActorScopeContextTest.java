package com.getlipa.eventstore.core.actor.cdi;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ActorScopeContextTest {

    private static final ActorId ACTOR_ID = ActorId.createDefault("actor-id");

    @Inject
    private BeanManager beanManager;

    @Test
    public void shouldActivate() {
        final var scope = ActorScopeContext.get(ACTOR_ID);
        Assertions.assertThrows(ContextNotActiveException.class, () -> beanManager.getContext(ActorScoped.class));
        Assertions.assertTrue(scope.compute(() -> beanManager.getContext(ActorScoped.class).isActive()));
        Assertions.assertThrows(ContextNotActiveException.class, () -> beanManager.getContext(ActorScoped.class));
    }
}