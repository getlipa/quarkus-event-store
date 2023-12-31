package com.getlipa.eventstore.core.actor;

import com.getlipa.eventstore.core.actor.cdi.ActorId;
import com.getlipa.eventstore.core.actor.cdi.ActorInterceptor;
import com.getlipa.eventstore.core.actor.cdi.ActorScope;
import com.getlipa.eventstore.core.actor.messaging.AnyMsg;
import com.getlipa.eventstore.core.actor.messaging.Msg;
import com.getlipa.eventstore.core.actor.messaging.MsgHandlerInvoker;
import com.getlipa.eventstore.core.actor.state.NoActor;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@RequiredArgsConstructor
public class ActorVerticle extends AbstractVerticle {

    private final Clock clock;

    private final ActorScope actorScope;

    private final MsgHandlerInvoker msgHandlerInvoker;

    private final int undeployInactivityThresholdMs = 60_000;

    private Instant timeOfLastMessage;

    private MessageConsumer<?> consumer;

    public static ActorVerticle createFor(MsgHandlerInvoker msgHandlerInvoker, ActorScope actorScope) {
        return new ActorVerticle(
                Clock.systemUTC(),
                actorScope,
                msgHandlerInvoker
        );
    }

    public static String address(ActorId actorId, long instanceId) {
        return String.format("%s:%s", actorId, instanceId);
    }

    @Override
    public void start(Promise<Void> startPromise) {
        consumer = vertx.eventBus().<AnyMsg>consumer(address(actorScope.getActorId(), instanceId()))
                .handler(this::handle);
        vertx.setTimer(undeployInactivityThresholdMs, timer -> undeployAfterInactivity());
        startPromise.complete();
    }

    @Override
    public void stop() {
        vertx.sharedData().getCounter(actorScope.getActorId().toString())
                .map(counter -> counter.compareAndSet(instanceId(), NoActor.COUNTER_VALUE))
                .onFailure(e -> log.warn("Detected stale actor while unregistering: {}", actorScope.getActorId()))
                .onComplete(result -> {
                    consumer.unregister()
                            .onSuccess(id -> log.info("Actor unregistered: {}", actorScope.getActorId()))
                            .onFailure(error -> log.error("Unable to unregister actor '{}': {}", actorScope.getActorId(), error.getMessage()));
                    actorScope.destroy();
                });
    }

    private void handle(Message<AnyMsg> message) {
        timeOfLastMessage = clock.instant();
        final var msg = message.body();
        final var result = actorScope.compute(() -> {
            log.trace("Handling actor message: {}", msg);
            try {
                return ActorInterceptor.invokeLocally(msgHandlerInvoker, msg);
            } catch (Exception e) {
                throw new IllegalStateException("Cannot invoke msg handler.", e);
            }
        });
        if (result != null && !(result instanceof com.google.protobuf.Message)) {
            // FIXME
            message.fail(0, "Unsupported msg handler return value: " + result);
        }
        final var deliveryOptions = new DeliveryOptions()
                .setCodecName(Msg.CODEC);
        message.reply(Msg.withPayload((com.google.protobuf.Message) result), deliveryOptions); // TODO
    }

    private void undeployAfterInactivity() {
        final var now = clock.instant();
        final var nextInactivityCheck = timeOfLastMessage.plus(undeployInactivityThresholdMs, ChronoUnit.MILLIS);
        final var timeToNextCheckMs = ChronoUnit.MILLIS.between(now, nextInactivityCheck);
        if (timeToNextCheckMs > 0) {
            log.trace("Actor '{}' is still active, undeployment postponed for {}ms", actorScope.getActorId(), timeToNextCheckMs);
            vertx.setTimer(timeToNextCheckMs, timer -> undeployAfterInactivity());
            return;
        }
        vertx.undeploy(deploymentID())
                .onSuccess(result -> log.trace("Actor undeployed due to inactivity: {}", actorScope.getActorId()))
                .onFailure(e -> log.error("Actor inactive but undeployment failed: {}", e));

    }

    private int instanceId() {
        return deploymentID().hashCode();
    }
}
