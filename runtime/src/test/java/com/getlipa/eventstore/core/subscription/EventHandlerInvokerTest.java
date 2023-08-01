package com.getlipa.eventstore.core.subscription;

import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.example.event.Example;
import com.getlipa.eventstore.core.subscription.cdi.EventHandler;
import com.google.protobuf.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class EventHandlerInvokerTest {

    private EventHandlerInvoker invoker;

    private Handlers handlers;

    @BeforeEach
    public void setup() {
        handlers = Mockito.spy(new Handlers());
        invoker = EventHandlerInvoker.create(handlers);
    }

    @Test
    public void test() {
        final var event = Event.builder().withPayload(Example.Simple.newBuilder().build());
        invoker.process((Event<Message>) (Object) event); // FIXME
        Mockito.verify(handlers).onSimple(event);
        Mockito.verify(handlers, Mockito.never()).onOther(Mockito.any());
    }

    public static class Handlers {

        @EventHandler
        public void onSimple(Event<Example.Simple> event) {

        }

        @EventHandler
        public void onOther(Event<Example.Other> event) {

        }
    }
}