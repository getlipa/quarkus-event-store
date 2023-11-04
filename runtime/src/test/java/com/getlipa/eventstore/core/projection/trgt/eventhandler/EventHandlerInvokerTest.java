package com.getlipa.eventstore.core.projection.trgt.eventhandler;

import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.projection.trgt.ProjectionStep;
import com.getlipa.eventstore.core.projection.trgt.ProjectionTarget;
import com.getlipa.eventstore.core.projection.trgt.context.ApplyContext;
import com.getlipa.eventstore.core.proto.Payload;
import com.getlipa.eventstore.subscriptions.Projections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

class EventHandlerInvokerTest {

    @Test
    void extractEventHandlers() {
        final var eventHandlers = EventHandlerInvoker.extractEventHandlers(Projection.class);

        assertEquals(1, eventHandlers.size());
        assertTrue(eventHandlers.containsKey(Projections.Event.class));
        assertEquals("theEventHandlerMethod", eventHandlers.get(Projections.Event.class).getName());
    }

    @Test
    void extractEventHandlers_invalid() {
        assertThrows(
                IllegalStateException.class,
                () -> EventHandlerInvoker.extractEventHandlers(InvalidProjection.class)
        );
    }

    @Test
    void invoke() {
        final var invoker = EventHandlerInvoker.create(Projection.class);
        final var projection = Mockito.spy(new Projection());
        final var event = (Event<Projections.Event>) Mockito.mock(Event.class);
        doReturn(Payload.create(Projections.Event.getDefaultInstance())).when(event).getPayload();

        invoker.invoke(projection, event);

        assertTrue(projection.invoked);
        Mockito.verify(projection).theEventHandlerMethod(event);
    }

    static class Projection {

        private boolean invoked = false;

        @Project
        public void theEventHandlerMethod(Event<Projections.Event> event) {
            invoked = true;
        }
    }

    static class InvalidProjection {

        @Project
        public void on(Object event) {

        }
    }
}