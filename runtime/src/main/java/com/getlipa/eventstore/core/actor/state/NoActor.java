package com.getlipa.eventstore.core.actor.state;

import com.getlipa.eventstore.core.actor.messaging.MessageDelivery;
import com.google.protobuf.Message;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class NoActor extends ActorState {

    private final Vertx vertx;

    public static final int COUNTER_VALUE = 0;

    @Override
    public <R extends Message> Future<R> process(MessageDelivery message) {
        final var actorId = message.getActorId();
        log.trace("Actor '{}' is not yet deployed.", actorId);
        return vertx.deployVerticle(String.format("actor:%s", actorId))
                .flatMap(deploymentId -> vertx.sharedData().getCounter(actorId.toString())
                        .flatMap(counter -> counter.compareAndSet(counter.get().result(), deploymentId.hashCode()))
                        .onSuccess(registered -> {
                            if (registered) {
                                log.trace("Successfully registered actor.");
                                return;
                            }
                            log.trace("Aborting deployment of actor '{}' - another instance has already been deployed.", actorId);
                            vertx.undeploy(deploymentId)
                                    .onSuccess(error -> log.error(
                                            "Actor undeployed."
                                    ))
                                    .onFailure(error -> log.error(
                                            "Unable to undeploy stale actor instance {}@{}: {}",
                                            actorId,
                                            deploymentId.hashCode(),
                                            error
                                    ));
                        }))
                .flatMap(result -> message.<R>deliver(vertx))
                .onFailure(error -> {
                    log.error("Unable to send {} to actor '{}': :{}", message.getMessage(), actorId, error);
                });
    }
}
