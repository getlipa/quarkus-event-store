package com.getlipa.eventstore.core.actor.cdi;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class ActorScopeContextRUNTIMETest {
/*
    private static final String ACTOR_ID = "actor-id";

    @Inject
    private Instance<SomeActor> actorInstance;

    @Inject
    private Instance<ActorDependency> dependencyInstance;

    @Inject
    private BeanManager beanManager;


    @Test
    public void shouldRunScoped() {
        final var actorId = ActorScopeContext.compute(ACTOR_ID, ActorScope::getActorId);
        Assertions.assertEquals(ACTOR_ID, actorId);
    }

    @Test
    public void shouldOverridePreviousContext() {
        final var secondActorId = ACTOR_ID + "-second";
        final var first = ActorScopeContext.compute(ACTOR_ID, ActorScope::getActorId);
        final var second = ActorScopeContext.compute(secondActorId, ActorScope::getActorId);
        Assertions.assertEquals(ACTOR_ID, first);
        Assertions.assertEquals(secondActorId, second);
    }

    @Test
    public void shouldFailWhenActorIsInjectedNotScoped() {
        var thrown = false;
        try {
            actorInstance.get();
        } catch (Exception e) {
            Assertions.assertInstanceOf(ContextNotActiveException.class, e.getCause());
            thrown = true;
        }
        Assertions.assertTrue(thrown);
    }

    @Test
    public void shouldCreateActorWhenScoped() {
        final var actor = ActorScopeContext.compute(ACTOR_ID, scope -> actorInstance.get());
        Assertions.assertEquals(ACTOR_ID, actor.actorId);
    }

    @Test
    public void shouldFailWhenActorDependencyIsInjectedNotScoped() {
        var thrown = false;
        try {
            dependencyInstance.get();
        } catch (Exception e) {
            Assertions.assertInstanceOf(ContextNotActiveException.class, e.getCause());
            thrown = true;
        }
        Assertions.assertTrue(thrown);
    }

    @Test
    public void shouldCreateActorDependencyWhenScoped() {
        final var dependency = ActorScopeContext.compute(ACTOR_ID, scope -> dependencyInstance.get());
       // Assertions.assertEquals(ACTOR_ID, dependency.actorId);
        //Assertions.assertInstanceOf(SomeActor.class, dependency.someActor);
    }

    @Test
    public void shouldActivate() {
       final var isActive = ActorScopeContext.compute(ACTOR_ID, scope -> beanManager.getContext(ActorScoped.class).isActive());
       Assertions.assertTrue(isActive);
    }

    @Actor(ACTOR_ID)
    public static class SomeActor {

        @Inject
        @ActorId
        String actorId;

        @Inject
        Instance<ActorDependency> actorDependencies;

    }

    @ActorScoped
    public static class ActorDependency {

        @Inject
        @ActorId
        String actorId;

       /*
       @Inject
        SomeActor someActor;

        */
/*
    }

 */
}