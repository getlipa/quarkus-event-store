package com.getlipa.eventstore.core.projection.subscription;

import com.getlipa.eventstore.core.Registry;
import com.getlipa.eventstore.core.projection.ProjectionMetadata;
import com.getlipa.eventstore.core.projection.checkpointing.CheckpointController;
import com.getlipa.eventstore.core.projection.checkpointing.ProjectionProgress;
import com.getlipa.eventstore.core.projection.extension.ExtensionFactory;
import com.getlipa.eventstore.core.projection.extension.ProjectionExtension;
import com.getlipa.eventstore.core.projection.projected.ProjectedLogs;
import com.getlipa.eventstore.core.projection.projector.ProjectorGateway;
import com.getlipa.eventstore.core.subscription.ManagedSubscription;
import com.getlipa.eventstore.core.subscription.SubscriptionManager;
import io.vertx.core.Future;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class SubscriptionExtension implements ProjectionExtension {

    private final ProjectionMetadata metadata;

    private final SubscriptionManager subscriptionManager;

    private final ProjectorGateway projectorGateway;

    private ManagedSubscription managedSubscription;

    public Future<Void> onStartup() {
        if (!metadata.getName().startsWith("$")) { // TODO: Use config!!!!
            return startListening();
        }
        log.trace("Subscription is disabled for projection: {}", metadata.getName());
        return Future.succeededFuture();
    }

    public Future<Void> onShutdown() {
        if (managedSubscription == null) {
            return Future.succeededFuture();
        }
        return managedSubscription.stop();
        /*

        subscription.cancel(); // FIXME: stop checkpointer
        final var listeningStopped = Event
                .withCausationId(subscriptionId)
                .withPayload(Projections.ListeningStopped.newBuilder().build());
        return projected.appendAny(listeningStopped)
                .onSuccess(result -> log.info("Subscription deregistered: {} / {}", metadata.getName(), subscriptionId))
                .onFailure(error -> log.warn(
                        "Cannot deregister subscription: {} / {} - {}",
                        metadata.getName(),
                        subscriptionId,
                        error.getMessage())
                )
                .mapEmpty();
                */
    }

    public Future<Void> startListening() {
        return subscriptionManager.subscribe(metadata, event -> projectorGateway.deliver(event).mapEmpty())
                .onSuccess(managedSubscription -> this.managedSubscription = managedSubscription)
                .mapEmpty();
    }
/*
    public Future<Void> startListeningOld() {
        final var listeningStarted = Event
                .withCausationId(subscriptionId)
                .withPayload(Projections.ListeningStarted.newBuilder().build());
        return projected.appendAny(listeningStarted)
                .map(projectionProgress -> checkpointController = checkpointingHandlerFactory.create(
                        metadata,
                        subscriptionId,
                        event -> projectorGateway.deliver(event).mapEmpty())
                )
                .map(checkpointController -> subscriptionManager.subscribe(
                        metadata.getSelector(),
                        checkpointController
                ))
                .map(subscription -> this.subscription = subscription)
                .onSuccess(result -> log.info("Subscription registered: {} / {}", metadata.getName(), subscriptionId))
                .mapEmpty();
    }

 */

    @ApplicationScoped
    @RequiredArgsConstructor
    public static class Factory implements ExtensionFactory {

        private final ProjectedLogs<ProjectionProgress> projectedLogs;

        private final SubscriptionManager subscriptionManager;

        private final CheckpointController.Factory checkpointingHandlerFactory;

        private final Registry<ProjectorGateway> projectorGatewayRegistry;

        @Override
        public ProjectionExtension create(ProjectionMetadata projectionMetadata) {
            return new SubscriptionExtension(
                    projectionMetadata,
                    //projectedLogs.get(projectionMetadata.getName()),
                    subscriptionManager,
                    //checkpointManagerFactory,
                   // checkpointingHandlerFactory,
                    projectorGatewayRegistry.lookup(projectionMetadata)

            );
        }
    }
}
