package com.getlipa.eventstore.core.actor.state;

import com.getlipa.eventstore.core.actor.cdi.ActorId;
import com.getlipa.eventstore.core.actor.cdi.ActorScope;
import com.getlipa.eventstore.core.actor.messaging.MessageDelivery;
import com.getlipa.eventstore.core.actor.messaging.Msg;
import com.google.protobuf.Message;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicReference;

@ExtendWith(MockitoExtension.class)
class ActorReadyTest {

    private static final ActorId ACTOR_ID = ActorId.create("actor-type", "actor-name");

    private static final long INSTANCE_ID = 0;

    @Mock
    private Vertx vertx;

    @Mock
    private EventBus eventBus;

    @Mock
    private ActorScope scope;

    @Mock
    private MessageDelivery messageDelivery;

    @Mock
    private DeliveryOptions deliveryOptions;
    @Mock
    private Message payload;

    private Msg<Message> msg;

    private ActorReady actorReady;

    @BeforeEach
    public void setup() {
        Mockito.doReturn(eventBus).when(vertx).eventBus();
        actorReady = new ActorReady(vertx, INSTANCE_ID);
        msg = Msg.withPayload(payload);

        Mockito.doReturn(ACTOR_ID).when(messageDelivery).getActorId();
        Mockito.doReturn(msg).when(messageDelivery).getMessage();
        Mockito.doReturn(deliveryOptions).when(messageDelivery).getDeliveryOptions();
    }

    @Test
    public void shouldRequestActorMessage() {
        Mockito.doReturn(Future.succeededFuture())
                .when(eventBus)
                .request(Mockito.anyString(), Mockito.any(), Mockito.any(DeliveryOptions.class));

        actorReady.process(messageDelivery);

        Mockito.verify(eventBus).request(
                Mockito.eq("actor-type:actor-name:0"),
                Mockito.same(msg),
                Mockito.same(deliveryOptions)
        );
        Mockito.verify(messageDelivery, Mockito.never()).deliver(vertx);
    }

    @Test
    public void shouldRetryAfterTemporaryFailures() {
        Mockito.doReturn(Future.failedFuture("temporary"))
                .when(eventBus)
                .request(Mockito.anyString(), Mockito.any(), Mockito.any(DeliveryOptions.class));

        actorReady.process(messageDelivery);

        AtomicReference<Handler<Long>> timerHandler = new AtomicReference<>();
        Mockito.verify(vertx).setTimer(Mockito.eq(500L), Mockito.assertArg(timerHandler::set));
        Mockito.verify(messageDelivery, Mockito.never()).deliver(Mockito.same(vertx));
        timerHandler.get().handle(0L);
        Mockito.verify(messageDelivery).deliver(Mockito.same(vertx));
    }
}