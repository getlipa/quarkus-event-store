package com.getlipa.eventstore.core.actor.messaging;

import com.getlipa.eventstore.core.actor.cdi.ActorId;
import com.getlipa.eventstore.core.actor.state.ActorState;
import com.google.protobuf.Message;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public class MessageDelivery {

    private final ActorId actorId;

    private final AnyMsg message;

    private final DeliveryOptions deliveryOptions;

    private final int maxRetry = 10; // TODO: config

    private int deliveryAttemptsCount = 0;

    public static MessageDelivery create(final AnyMsg payload, final ActorId actorId) {
        return new MessageDelivery(
                actorId,
                payload,
                new DeliveryOptions().setCodecName(Msg.CODEC)
        );
    }

    public <R extends Message> Future<R> deliver(Vertx vertx) {
        if (deliveryAttemptsCount > maxRetry) {
            return Future.failedFuture("Max retry count exceeded: " + maxRetry);
        }
        deliveryAttemptsCount++;
        log.trace("Attempt {} to send {} to actor '{}'", deliveryAttemptsCount, message, actorId);
        return ActorState.determine(actorId, vertx)
                .<R>flatMap(state -> state.process(this))
                .onFailure(error -> log.error("ERROR occurred: {}", error.getMessage()));
    }
}
