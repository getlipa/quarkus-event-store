package com.getlipa.eventstore.core.projection.trgt.middleware;

import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.projection.trgt.ProjectionStep;
import com.getlipa.eventstore.core.projection.trgt.ProjectionTarget;
import com.getlipa.eventstore.core.projection.trgt.eventhandler.EventHandlerInvoker;
import com.getlipa.eventstore.core.projection.trgt.eventhandler.Project;
import com.getlipa.eventstore.core.projection.trgt.context.AbstractContext;
import com.getlipa.eventstore.core.projection.trgt.context.InitializeContext;
import com.getlipa.eventstore.core.projection.trgt.context.ApplyContext;
import com.getlipa.eventstore.core.proto.Payload;
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

// FIXME: Integration Test -> naming?
class MiddlewareTest {

    private ProjectionTarget<Projection> projectionTarget;

    private ProjectionStep<Projection> projectionStep;

    private StringBuilder middlewareTrace;

    @BeforeEach
    void setUp() {
        middlewareTrace = new StringBuilder();
        projectionTarget = new ProjectionTarget<>(
                null,
                null,
                List.of(
                        new TracingMiddleware(middlewareTrace, "B"),
                        new TracingMiddleware(middlewareTrace, "A")
                ),
                EventHandlerInvoker.create(Projection.class),
                () -> new Projection(middlewareTrace)
        );
    }

    @Test
    void init() {
        projectionTarget.initialized();

        assertEquals("init-A;init-B;", middlewareTrace.toString());
    }

    @Test
    void project() {
        final var event = Mockito.mock(Event.class);
        doReturn(Payload.create(Projections.Event.getDefaultInstance())).when(event).getPayload();
        projectionTarget.apply(event);

        assertEquals("init-A;init-B;project-A;project-B;target", middlewareTrace.toString());
    }

    @RequiredArgsConstructor
    public static class Projection {

        private final StringBuilder middlewareTrace;

        @Project
        public void on(Event<Projections.Event> event) {
            middlewareTrace.append("target");
        }
    }

    @RequiredArgsConstructor
    static class TracingMiddleware extends ProjectionTarget.Middleware<Projection> {

        private final StringBuilder middlewareTrace;

        private final String name;

        private Future<Projection> trace(AbstractContext<Projection> context, String operation) {
            middlewareTrace.append(String.format("%s-%s;", operation, name));
            return context.proceed();
        }

        @Override
        protected Future<Projection> init(InitializeContext<Projection> context) {
            return trace(context, "init");
        }

        @Override
        public Future<Projection> project(ApplyContext<Projection> initContext) {
            return trace(initContext, "project");
        }
    }
}