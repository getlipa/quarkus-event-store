package com.getlipa.eventstore.core.projection.checkpointing;

import com.getlipa.eventstore.core.event.AnyEvent;
import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.projection.ProjectionMetadata;
import com.getlipa.eventstore.core.projection.projected.ProjectedLog;
import com.getlipa.eventstore.core.projection.projected.ProjectedLogs;
import com.getlipa.eventstore.subscriptions.Projections;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class CheckpointController implements AnyEvent.Handler<Void> {

    private final long NO_TIMER_ID = -1;

    private final Set<Long> currentlyProcessingEventPositions = new HashSet<>();

    private final Vertx vertx;

    @Getter
    private final UUID id;

    private final ProjectedLog<ProjectionProgress> projectedLog;

    private final ProjectionMetadata projectionMetadata;

    private final AnyEvent.Handler<?> handler;

    private long timerId = NO_TIMER_ID;

    private void onEventProcessingCompleted(AnyEvent event) {
        currentlyProcessingEventPositions.remove(event.getPosition());
    }

    public long tip() {
        if (currentlyProcessingEventPositions.isEmpty()) {
            return Long.MAX_VALUE;
        }
        return Collections.min(currentlyProcessingEventPositions) - 1;
    }


    public void start() {
        timerId = vertx.setPeriodic(1000, id -> {
            checkpoint();
        });
    }

    public void stop() {
        vertx.cancelTimer(timerId);
        log.trace("Checkpointing job removed: {} / {}", projectionMetadata.getName(), id);
    }

    private void checkpoint() {
        final var event = Event
                .withCausationId(id)
                .withPayload(Projections.CheckpointReached.newBuilder()
                .setTip(tip())
                .build());
        projectedLog.appendAny(event)
                .onSuccess(progress -> log.info(
                        "Checkpoint persisted: {} / {}",
                        projectionMetadata.getName(),
                        progress.tip())
                )
                .onFailure(error -> log.error("Unable to persist checkpoint: {}", error.getMessage()));
    }

    @Override
    public Future<Void> handle(AnyEvent event) {
        if (timerId == NO_TIMER_ID) {
            start();
        }
        currentlyProcessingEventPositions.add(event.getPosition());
        return handler.handle(event)
                .onComplete(response -> onEventProcessingCompleted(event))
                .mapEmpty();
    }

    @ApplicationScoped
    @RequiredArgsConstructor
    public static class Factory {

        private final Vertx vertx;

        private final ProjectedLogs<ProjectionProgress> projectedLogs;

        public CheckpointController create(ProjectionMetadata projectionMetadata, UUID id, AnyEvent.Handler<?> handler) {
            return new CheckpointController(
                    vertx,
                    id,
                    projectedLogs.get(projectionMetadata.getName()),
                    projectionMetadata,
                    handler
            );
        }

    }
}
