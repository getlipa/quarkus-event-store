package com.getlipa.eventstore.aggregate.hydration;

import com.getlipa.eventstore.aggregate.context.Context;
import com.getlipa.eventstore.aggregate.middleware.AggregateMiddleware;
import com.getlipa.eventstore.identifier.Id;
import com.getlipa.eventstore.event.Event;
import com.getlipa.eventstore.hydration.Hydrator;
import com.getlipa.eventstore.hydration.eventhandler.EventHandlerInvoker;
import com.getlipa.eventstore.hydration.eventhandler.Apply;
import com.getlipa.eventstore.aggregate.middleware.steps.AbstractStep;
import com.getlipa.eventstore.aggregate.middleware.steps.InitializeStep;
import com.getlipa.eventstore.aggregate.middleware.steps.ApplyStep;
import com.getlipa.eventstore.event.payload.Payload;
import com.getlipa.eventstore.subscriptions.Projections;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class AggregateHydratorTest {

    private Hydrator<Projection> hydrator;

    private StringBuilder middlewareTrace;

    @BeforeEach
    void setUp() {
        middlewareTrace = new StringBuilder();

        hydrator = AggregateHydrator.chained(
                mock(Id.class),
                mock(Context.class),
                EventHandlerInvoker.create(Projection.class).createHydrator(new Projection(middlewareTrace)),
                List.of(
                        new TracingAggregateMiddleware(middlewareTrace, "B"),
                        new TracingAggregateMiddleware(middlewareTrace, "A")
                )
        );
    }

    @Test
    void init() {
        hydrator.initialized();

        assertEquals("init-A;init-B;", middlewareTrace.toString());
    }

    @Test
    void apply() {
        final var event = Mockito.mock(Event.class);
        doReturn(Payload.create(Projections.Event.getDefaultInstance())).when(event).getPayload();
        hydrator.apply(event);

        assertEquals("apply-A;apply-B;target", middlewareTrace.toString());
    }

    @RequiredArgsConstructor
    public static class Projection {

        private final StringBuilder middlewareTrace;

        @Apply
        public void on(Event<Projections.Event> event) {
            middlewareTrace.append("target");
        }
    }

    @RequiredArgsConstructor
    static class TracingAggregateMiddleware extends AggregateMiddleware<Projection> {

        private final StringBuilder middlewareTrace;

        private final String name;

        private Future<Projection> trace(AbstractStep<Projection> context, String operation) {
            middlewareTrace.append(String.format("%s-%s;", operation, name));
            return context.proceed();
        }

        @Override
        public Future<Projection> init(InitializeStep<Projection> context) {
            return trace(context, "init");
        }

        @Override
        public Future<Projection> project(ApplyStep<Projection> initContext) {
            return trace(initContext, "apply");
        }
    }
}