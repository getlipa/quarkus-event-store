package com.getlipa.eventstore.core.actor.state;

import com.getlipa.eventstore.core.actor.ActorVerticle;
import com.getlipa.eventstore.core.actor.messaging.MessageDelivery;
import com.getlipa.eventstore.core.actor.messaging.Result;
import com.getlipa.eventstore.core.proto.Payload;
import com.google.protobuf.Message;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ActorReady extends ActorState {

    private final Vertx vertx;

    private final long instanceId;

    @Override
    public <R extends Message> Future<R> process(MessageDelivery message) {
        final  var address = ActorVerticle.address(message.getActorId(), instanceId);
        log.trace("Dispatching {} to actor '{}'", message.getMessage(), address);
        return vertx.eventBus().<Result<R>>request(address, message.getMessage(), message.getDeliveryOptions())
                .map(io.vertx.core.eventbus.Message::body)
                .map(Result::getPayload)
                .map(Payload::get)
                .onSuccess(reply -> {
                    log.trace("Actor replied: {}", reply);
                })
                .onFailure(error -> {
                    if (!isTemporary(error)) {
                        log.error("Permanent failure in message delivery: {}", error.getMessage());
                        return;
                    }
                    log.error("Temporary failure in message delivery: {}", error.getMessage());
                    vertx.setTimer(500, timer -> message.deliver(vertx));
                });
    }

    boolean isTemporary(Throwable error) {
        return true; // FIXME
    }
}
