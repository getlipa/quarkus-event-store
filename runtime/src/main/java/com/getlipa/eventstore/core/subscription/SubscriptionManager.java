package com.getlipa.eventstore.core.subscription;

import com.getlipa.eventstore.core.event.AnyEvent;
import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.event.selector.Selector;
import com.getlipa.eventstore.core.projection.ProjectionMetadata;
import com.getlipa.eventstore.core.projection.checkpointing.CheckpointController;
import com.getlipa.eventstore.core.projection.checkpointing.ProjectionProgress;
import com.getlipa.eventstore.core.projection.projected.ProjectedLogs;
import com.getlipa.eventstore.subscriptions.Projections;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@ApplicationScoped
public class SubscriptionManager {

    private final Set<AnyEvent.Handler<Void>> handlers = new LinkedHashSet<>();

    private final CheckpointController.Factory checkpointingHandlerFactory;

    private final ProjectedLogs<ProjectionProgress> progressProjectedLogs;


    public void on(@ObservesAsync EventAppended eventAppended) {
        handlers.forEach(handler -> handler.handle(eventAppended.getEvent()));
    }

    public Future<ManagedSubscription> subscribe(ProjectionMetadata projectionMetadata, AnyEvent.Handler<Void> handler) {
        final var subscriptionId = UUID.randomUUID();
        final var checkpointController = checkpointingHandlerFactory.create(projectionMetadata, subscriptionId, handler);
        final var subscription = new ManagedSubscription(
                progressProjectedLogs.get(projectionMetadata.getName()),
                projectionMetadata,
                checkpointController
        );
        return subscription.register(this)
                .map(vd -> subscription);
    }

    public Subscription subscribe(Selector selector, AnyEvent.Handler<?> handler) {
        handlers.add(event -> {
            if (selector.matches(event)) {
                return handler.handle(event).mapEmpty();
            }
            return Future.succeededFuture();
        });
        return () -> handlers.remove(handler);
    }
}
