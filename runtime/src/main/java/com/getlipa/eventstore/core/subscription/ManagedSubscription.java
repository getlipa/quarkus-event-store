package com.getlipa.eventstore.core.subscription;

import com.getlipa.eventstore.core.event.AnyEvent;
import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.projection.ProjectionMetadata;
import com.getlipa.eventstore.core.projection.checkpointing.CheckpointController;
import com.getlipa.eventstore.core.projection.checkpointing.ProjectionProgress;
import com.getlipa.eventstore.core.projection.projected.ProjectedLog;
import com.getlipa.eventstore.subscriptions.Projections;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class ManagedSubscription {

    private final ProjectedLog<ProjectionProgress> projectedLog;

    private final ProjectionMetadata metadata;

    private final CheckpointController checkpointController;

    private Subscription subscription;

    public Future<Void> register(SubscriptionManager subscriptionManager) {
        final var subscriptionId = checkpointController.getId();
        final var listeningStarted = Event
                .withCausationId(subscriptionId)
                .withPayload(Projections.ListeningStarted.newBuilder().build());
        return projectedLog.appendAny(listeningStarted)
                .onSuccess(result -> log.info("Subscription registered: {} / {}", metadata.getName(), metadata))
                .map(projectionProgress -> subscriptionId)
                .onSuccess(result -> {
                    checkpointController.start();
                    subscription = subscriptionManager.subscribe(
                            metadata.getSelector(),
                            event -> checkpointController.handle(event).mapEmpty()
                    );
                })
                .mapEmpty();
    }

    public Future<Void> stop() {
        final var subscriptionId = checkpointController.getId();
        subscription.cancel();
        checkpointController.stop();
        final var listeningStopped = Event
                .withCausationId(subscriptionId)
                .withPayload(Projections.ListeningStopped.newBuilder().build());
        return projectedLog.appendAny(listeningStopped)
                .onSuccess(result -> log.info("Subscription deregistered: {} / {}", metadata.getName(), subscriptionId))
                .onFailure(error -> log.warn(
                        "Cannot deregister subscription: {} / {} - {}",
                        metadata.getName(),
                        subscriptionId,
                        error.getMessage())
                )
                .mapEmpty();
    }
}
