package com.getlipa.eventstore.core.actor.cdi;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ActorScopedTest {

    private static final ActorId ACTOR_ID = ActorId.create("actor-type", "actor-name");

    @Inject
    private Instance<Bean> bean;

    @Test
    public void shouldCreateOnePerScope() {
        final var firstScope = ActorScopeContext.get(ACTOR_ID);
        final var firstInstance = firstScope.compute(() -> {
            final var actor = bean.get();
            Assertions.assertEquals(ACTOR_ID.getType(), actor.getActorType());
            Assertions.assertEquals(ACTOR_ID.getName(), actor.getActorName());
            return actor.self();
        });
        Assertions.assertSame(firstInstance, firstScope.compute(() -> bean.get().self()));

        final var secondActorId = ActorId.createDefault(ACTOR_ID.getType() + "-second");
        final var secondScope = ActorScopeContext.get(secondActorId);
        final var secondInstance = secondScope.compute(() -> {
            final var actor = bean.get();
            Assertions.assertEquals(secondActorId.getType(), actor.getActorType());
            Assertions.assertEquals(secondActorId.getName(), actor.getActorName());
            return actor;
        });
        Assertions.assertSame(secondInstance, secondScope.compute(() -> bean.get()));
        Assertions.assertNotSame(firstInstance, secondInstance);
    }

    @Test
    public void shouldFailWhenNotScoped() {
        Assertions.assertThrows(ContextNotActiveException.class, () -> bean.get().self());
    }

    @ActorScoped
    @RequiredArgsConstructor
    public static class Bean {

        private final ActorId actorId;

        public Bean self() {
            return this;
        }

        public String getActorType() {
            return actorId.getType();
        }

        public String getActorName() {
            return actorId.getName();
        }
    }
}