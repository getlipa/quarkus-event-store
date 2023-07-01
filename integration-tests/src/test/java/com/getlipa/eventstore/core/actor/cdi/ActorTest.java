package com.getlipa.eventstore.core.actor.cdi;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ActorTest {

    private static final String ACTOR_TYPE = "test";

    private static final ActorId FIRST_ACTOR_ID = ActorId.create(ACTOR_TYPE, "first-actor-id");

    private static final ActorId SECOND_ACTOR_ID = ActorId.create(ACTOR_TYPE, "second-actor-id");

    @Inject
    @Actor.Qualifier
    private Instance<ActorBean> actorBeans;

    private ActorScope firstScope;

    private ActorScope secondScope;

    private ActorBean firstActor;

    private ActorBean secondActor;

    @BeforeEach
    public void setup() {
        firstScope = ActorScopeContext.get(FIRST_ACTOR_ID);
        firstActor = firstScope.compute(() -> actorBeans.get().withoutClientProxies());
        secondScope = ActorScopeContext.get(SECOND_ACTOR_ID);
        secondActor = secondScope.compute(() -> actorBeans.get().withoutClientProxies());
    }

    @Test
    public void shouldFailWhenNotScoped() {
        final var actor = actorBeans.get();
        Assertions.assertThrows(ContextNotActiveException.class, actor::withoutClientProxies);
    }

    @Test
    public void shouldCreateOnePerScope() {
        Assertions.assertEquals(FIRST_ACTOR_ID.getType(), firstScope.compute(() -> firstActor.actorId.getType()));
        Assertions.assertEquals(FIRST_ACTOR_ID.getName(), firstScope.compute(() -> firstActor.actorId.getName()));
        Assertions.assertSame(firstActor, firstScope.compute(() -> actorBeans.get().withoutClientProxies()));
        Assertions.assertEquals(SECOND_ACTOR_ID.getType(), secondScope.compute(() -> secondActor.actorId.getType()));
        Assertions.assertEquals(SECOND_ACTOR_ID.getName(), secondScope.compute(() -> secondActor.actorId.getName()));
        Assertions.assertSame(secondActor, secondScope.compute(() -> actorBeans.get().withoutClientProxies()));
        Assertions.assertNotSame(firstActor, secondActor);
    }

    @Test
    public void shouldCreateOneDependencyPerScope() {
        final var firstDependency = firstScope.compute(() -> actorBeans.get().withoutClientProxies().dependency.withoutClientProxies());
        Assertions.assertNotNull(firstDependency);
        Assertions.assertSame(firstDependency, firstScope.compute(() -> actorBeans.get().withoutClientProxies().dependency.withoutClientProxies()));

        final var secondDependency = secondScope.compute(() -> actorBeans.get().withoutClientProxies().dependency.withoutClientProxies());
        Assertions.assertNotNull(secondDependency);
        Assertions.assertSame(secondDependency, secondScope.compute(() -> actorBeans.get().withoutClientProxies().dependency.withoutClientProxies()));

        Assertions.assertNotSame(firstDependency, secondDependency);
    }

    @Actor(ACTOR_TYPE)
    @RequiredArgsConstructor
    public static class ActorBean {

        private final ActorId actorId;

        private final ActorDependency dependency;

        public ActorBean withoutClientProxies() {
            return this;
        }
    }

    @ActorScoped
    @RequiredArgsConstructor
    public static class ActorDependency {

        public ActorDependency withoutClientProxies() {
            return this;
        }
    }
}