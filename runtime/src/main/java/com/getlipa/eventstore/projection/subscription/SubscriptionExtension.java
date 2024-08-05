package com.getlipa.eventstore.projection.subscription;

import com.getlipa.eventstore.projection.Companion;
import com.getlipa.eventstore.projection.extension.ExtensionFactory;
import com.getlipa.eventstore.projection.projector.Projector;
import com.getlipa.eventstore.projection.subscription.config.SubscriptionConfig;
import com.getlipa.eventstore.projection.subscription.config.SubscriptionConfigs;
import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.job.JobRunner;
import com.getlipa.eventstore.job.Job;
import com.getlipa.eventstore.projection.ProjectionMetadata;
import com.getlipa.eventstore.projection.extension.ProjectionExtension;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class SubscriptionExtension implements ProjectionExtension {

    private final Promise<Void> jobCompleted = Promise.promise();

    private final PendingEvents pendingEvents = PendingEvents.create();

    private final ProjectionMetadata metadata;

    private final SubscriptionManager subscriptionManager;

    private final JobRunner jobRunner;

    private final Projector projector;

    private final SubscriptionConfig.Config config;

    private Future<Job> job;

    public Future<Void> onStartup() {
        if (config.enabled()) {
            job = listen();
            return job.mapEmpty();
        }
        log.trace("Subscription is disabled for projection: {}", metadata.getName());
        return Future.succeededFuture();
    }

    public Future<Void> onShutdown() {
        if (job == null) {
            return Future.succeededFuture();
        }
        jobCompleted.complete();
        return job.flatMap(Job::completed)
                .mapEmpty();
    }

    public Future<Job> listen() {
        return jobRunner.create()
                .reportTo("$projection", metadata.getName())
                .checkpointIntervalMs(config.checkpointIntervalMs())
                .checkpointFrom(pendingEvents::checkpoint)
                .start(job -> {
                    final var subscription = subscriptionManager.subscribe(metadata.getContext().createSelector(), this::handleEvent);
                    return jobCompleted.future()
                            .andThen(vd -> subscription.stop())
                            .flatMap(vd -> pendingEvents.cleared());
                });
    }

    Future<Object> handleEvent(AnyEvent event) {
        pendingEvents.add(event);
        return projector.project(event)
                .mapEmpty() // FIXME
                .onComplete(result -> pendingEvents.remove(event));
    }

    @ApplicationScoped
    @RequiredArgsConstructor
    public static class Factory implements ExtensionFactory {

        private final SubscriptionManager subscriptionManager;

        private final Companion<Projector> projectorGatewayCompanion;

        private final JobRunner jobRunner;

        private final SubscriptionConfigs configs;

        @Override
        public ProjectionExtension create(ProjectionMetadata projectionMetadata) {
            return new SubscriptionExtension(
                    projectionMetadata,
                    subscriptionManager,
                    jobRunner,
                    projectorGatewayCompanion.lookup(projectionMetadata),
                    configs.all().get(projectionMetadata.getName()).subscription()
            );
        }
    }
}
