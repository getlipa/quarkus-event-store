package com.getlipa.eventstore.projection.projector.deployment;


import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.projection.projector.scope.ProjectorScope;
import com.getlipa.eventstore.projection.projector.EventCodec;
import com.getlipa.eventstore.hydration.Hydrator;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@RequiredArgsConstructor
public class ProjectorDeployment<T> extends AbstractVerticle {

    // TODO: Better codec handling?
    public static final DeliveryOptions DELIVERY_OPTIONS = new DeliveryOptions().setCodecName(EventCodec.NAME);

    static Vertx vertxOverride;

    private final Clock clock;

    private final DeploymentManager deploymentManager;

    private final ProjectorScope projectorScope;

    private final Hydrator<T> hydrator;

    private final int undeployInactivityThresholdMs = 60_000;

    private Instant timeOfLastMessage;

    private MessageConsumer<?> consumer;

    private DeploymentId deploymentId;

    public static <T> ProjectorDeployment<T> createFor(
            DeploymentManager deploymentManager,
            Hydrator<T> hydrator,
            ProjectorScope projectorScope
    ) {
        final var projector = new ProjectorDeployment<>(
                Clock.systemUTC(),
                deploymentManager,
                projectorScope,
                hydrator
        );
        projector.timeOfLastMessage = projector.clock.instant();
        return projector;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        if (vertxOverride != null) {
            vertx = vertxOverride;
        }
        super.init(vertx, context);
    }

    @Override
    public void start(Promise<Void> startPromise) {
        deploymentId = DeploymentId.create(projectorScope.getId(), deploymentID());
        consumer = vertx.eventBus().<AnyEvent>consumer(deploymentId.toString())
                .handler(this::handle);
        log.trace("Listening on address: {}", deploymentId);
        vertx.setTimer(undeployInactivityThresholdMs, timer -> undeployAfterInactivity());
        startPromise.complete();
    }

    @Override
    public void stop() {
        deploymentManager.unregister(deploymentId)
                .onComplete(result -> consumer.unregister()
                        .onSuccess(id -> log.info("Projector unregistered: {}", deploymentId))
                        .onFailure(error -> log.error("Unable to unregister projector '{}': {}", deploymentId, error.getMessage()))
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
        MDC.put("projector", deploymentId.toString());
        timeOfLastMessage = clock.instant();
        final var msg = message.body();
        projectorScope.compute(() -> {
                    log.trace("Handling projector message: {}", msg);
                    return hydrator.initialized()
                            .flatMap(target -> hydrator.apply(message.body()));
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
                    deploymentId,
                    timeToNextCheckMs
            );
            vertx.setTimer(timeToNextCheckMs, timer -> undeployAfterInactivity());
            return;
        }
        log.trace("Projector has not been active for {}ms: {}", undeployInactivityThresholdMs, deploymentId);
        vertx.undeploy(deploymentID())
                .onSuccess(ok -> log.trace("Projector undeployed: {}", deploymentId))
                .onFailure(e -> log.error(
                        "Undeployment of inactive projector failed: {} - {}",
                        deploymentId,
                        e.toString()
                ));
    }
}
