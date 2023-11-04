package com.getlipa.eventstore.core.projection.projector;


import com.getlipa.eventstore.core.event.AnyEvent;
import com.getlipa.eventstore.core.projection.projector.instance.InstanceManager;
import com.getlipa.eventstore.core.projection.trgt.ProjectionTarget;
import com.getlipa.eventstore.core.projection.projector.scope.ProjectorScope;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@RequiredArgsConstructor
public class Projector<T> extends AbstractVerticle {

    public static final String CODEC = "event";
    // TODO: Better codec handling?
    public static final DeliveryOptions DELIVERY_OPTIONS = new DeliveryOptions().setCodecName(CODEC);

    static Vertx vertxOverride;

    private final Clock clock;

    private final InstanceManager instanceManager;

    private final ProjectorScope projectorScope;

    private final ProjectionTarget<T> projectionTarget; // FIXME: Use ProjectedStream to resolve loading issues.

    private final int undeployInactivityThresholdMs = 60_000;

    private Instant timeOfLastMessage;

    private MessageConsumer<?> consumer;

    private Id projectorId;

    public static <T> Projector<T> createFor(
            InstanceManager instanceManager,
            ProjectionTarget<T> projectionTarget,
            ProjectorScope projectorScope
    ) {
        return new Projector<>(
                Clock.systemUTC(),
                instanceManager,
                projectorScope,
                projectionTarget
        );
    }

    @Override
    public void init(Vertx vertx, Context context) {
        if(vertxOverride != null) {
            vertx = vertxOverride;
        }
        super.init(vertx, context);
    }

    @Override
    public void start(Promise<Void> startPromise) {
        projectorId = Id.create(projectorScope.getId(), deploymentID());
        consumer = vertx.eventBus().<AnyEvent>consumer(projectorId.toString())
                .handler(this::handle);
        vertx.setTimer(undeployInactivityThresholdMs, timer -> undeployAfterInactivity());
        startPromise.complete();
    }

    @Override
    public void stop() {
        instanceManager.unregister(projectorId)
                .onComplete(result -> consumer.unregister()
                        .onSuccess(id -> log.info("Projector unregistered: {}", projectorId))
                        .onFailure(error -> log.error("Unable to unregister projector '{}': {}", projectorId, error.getMessage()))
                        .onComplete(r -> projectorScope.destroy()));

    }

    private void handle(Message<AnyEvent> message) {
        try {
           doHandle(message);
        } catch (Exception e) {
            // TODO: Better error handling
            message.fail(0, e.getMessage());
        }
    }

    private void doHandle(Message<AnyEvent> message) {
        MDC.put("projector", projectorId.toString());
        timeOfLastMessage = clock.instant();
        final var msg = message.body();
        projectorScope.compute(() -> {
                    log.trace("Handling projector message: {}", msg);
                    return projectionTarget.initialized()
                            .flatMap(target -> projectionTarget.apply(message.body()));
                })
                .onComplete(result -> message.reply(msg, DELIVERY_OPTIONS));
    }

    private void undeployAfterInactivity() {
        final var now = clock.instant();
        final var nextInactivityCheck = timeOfLastMessage.plus(undeployInactivityThresholdMs, ChronoUnit.MILLIS);
        final var timeToNextCheckMs = ChronoUnit.MILLIS.between(now, nextInactivityCheck);
        if (timeToNextCheckMs > 0) {
            log.trace(
                    "Projector is still active, undeployment postponed for {}ms: {}",
                    projectorId,
                    timeToNextCheckMs
            );
            vertx.setTimer(timeToNextCheckMs, timer -> undeployAfterInactivity());
            return;
        }
        log.trace("Projector has not been active for {}ms: {}", undeployInactivityThresholdMs, projectorId);
        vertx.undeploy(deploymentID())
                .onSuccess(ok -> log.trace("Projector undeployed: {}", projectorId))
                .onFailure(e -> log.error(
                        "Undeployment of inactive projector failed: {} - {}",
                        projectorId,
                        e.toString()
                ));
    }

    @Getter
    @EqualsAndHashCode
    @RequiredArgsConstructor
    public static class Id {

        private final ProjectionTarget.Id targetId;

        private final long instanceId;

        public static Id unregistered(ProjectionTarget.Id targetId) {
            return create(targetId, 0);
        }

        public static Id create(ProjectionTarget.Id targetId, String deploymentId) {
            return create(targetId, Integer.toUnsignedLong(deploymentId.hashCode()));
        }

        public static Id create(ProjectionTarget.Id targetId, long instanceId) {
            return new Id(targetId, instanceId);
        }

        public String toString() {
            return String.format("%s+%s", targetId, instanceId);
        }
    }
}
