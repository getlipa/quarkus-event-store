package com.getlipa.eventstore.core.actor;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class ActorInterceptorRUNTIMETest {
/*
    @Inject
    private SomeActor actor;

    @Inject
    private Vertx vertx;

    @Inject
    private PayloadParser payloadParser;

    @BeforeEach
    public void setup() {
        vertx.eventBus().registerCodec(new CommandCodec(payloadParser));
        vertx.eventBus().registerCodec(new ResultCodec(payloadParser));
    }

    @Test
    public void test() {
        actor.some(Command.withPayload(Example.Simple.newBuilder().setData("gugus").build()));
    }


    @Actor("some")
    private static class SomeActor {

        SomeAggregate aggregate;

        public void some(Command<Example.Simple> simple) {
            System.out.println("GUGUS");
            final var event = simple.createEvent()
                    .withPayload(Example.Other.newBuilder().build());

        }

        public void other(Command<Example.Other> simple) {
            //aggregate.append(Event.withPayload(null));
        }
    }

    private static class SomeAggregate extends AggregateProjection {


    }

 */
}