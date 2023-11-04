package com.getlipa.eventstore.core.projection.checkpointing;

import com.getlipa.eventstore.core.EventStore;
import com.getlipa.eventstore.core.Registry;
import com.getlipa.eventstore.core.event.AnyEvent;
import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.projection.ProjectionMetadata;
import com.getlipa.eventstore.core.projection.projected.ProjectedLog;
import com.getlipa.eventstore.core.projection.projected.ProjectedLogs;
import com.getlipa.eventstore.core.projection.projector.ProjectorGateway;
import com.getlipa.eventstore.core.proto.ProtoUtil;
import com.getlipa.eventstore.core.stream.reader.cursor.Cursor;
import com.getlipa.eventstore.core.util.InstanceId;
import com.getlipa.eventstore.subscriptions.Projections;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.PriorityQueue;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class CatchUpProcess implements AnyEvent.Handler<Void> {

    private final EventStore eventStore;

    private final ProjectionMetadata metadata;

    private final ProjectedLog<ProjectionProgress> projectedLog;

    private final int maxBatchSize = 10; // TODO: config

    private final UUID id;

    private final ProjectorGateway projectorGateway;

    private final CheckpointController.Factory checkpointingHandlerFactory;

    private final PriorityQueue<Promise<Void>> queuedProcesses = new PriorityQueue<>();

    private int incompleteProcesses = 0;

    private boolean isRunning = false;

    private Future<Void> isCompleted;

    public Future<Void> start() {
        isRunning = true;
        final var handler = checkpointingHandlerFactory.create(metadata, id, this);
        isCompleted = Future.future(allCompleted -> projectedLog.get(true)
                        .flatMap(this::acquireLock)
                        .onSuccess(projectionProgress -> eventStore.stream(metadata.getSelector())
                                .readForward()
                                .from(Cursor.position(projectionProgress.getCatchUpTip()))
                                .until(Cursor.position(projectionProgress.getCatchUpTip()))
                                .forEach(event -> Future.future(queuedProcesses::add)
                                        .andThen(vd -> incompleteProcesses++)
                                        .flatMap(vd -> handler.handle(event))
                                        .andThen(vd -> {
                                            if (--incompleteProcesses == 0) {
                                                allCompleted.complete();
                                            }
                                        })
                                        .onComplete(response -> processNext())
                                )))
                .flatMap(result -> releaseLock());

        for (int i = 0; i < maxBatchSize; i++) {
            processNext();
        }
        return isCompleted;
    }

    private void processNext() {
        if (isRunning && !queuedProcesses.isEmpty()) {
            queuedProcesses.poll().complete();
        }
    }

    Future<ProjectionProgress> acquireLock(final ProjectionProgress projectionProgress) {
        log.trace("Trying to acquire lock on catch-up: {} / {}", metadata.getName(), projectedLog.getId());
        if (projectionProgress.catchingUp()) {
            log.trace("Catch-up process is already running : {} / {}", metadata.getName(), projectedLog.getId());
            return Future.failedFuture("Catch-up process is already running.");
        }
        final var catchUpStarted = Event.withCausationId(id)
                .withPayload(Projections.CatchUpStarted.newBuilder()
                .setId(ProtoUtil.convert(id))
                .build());
        return projectedLog.append(catchUpStarted)
                .recover(err -> projectedLog.get())
                .onFailure(error -> log.trace("Catch-up lock has already been acquired by another instance."))
                .onSuccess(error -> log.trace("Catch-up lock acquired."));
    }

    Future<Void> releaseLock() {
        isRunning = false;
        final var catchUpCompleted = Event.withCausationId(id)
                .withPayload(Projections.CatchUpCompleted.newBuilder()
                        .setId(ProtoUtil.convert(id))
                        .build());
        return projectedLog.appendAny(catchUpCompleted)
                .mapEmpty();
    }

    @Override
    public Future<Void> handle(AnyEvent event) {
        return projectorGateway.deliver(event)
                .mapEmpty();
    }

    @ApplicationScoped
    @RequiredArgsConstructor
    public static class Factory {

        private final EventStore eventStore;

        private final Registry<ProjectorGateway> projectorGatewayRegistry;

        private final InstanceId instanceId;

        private final CheckpointController.Factory factory;

        private final ProjectedLogs<ProjectionProgress> projectedLogs;

        public CatchUpProcess create(ProjectionMetadata metadata) {
            final var id = instanceId.getUuid();
            return new CatchUpProcess(
                    eventStore,
                    metadata,
                    projectedLogs.get(metadata.getName()),
                    id,
                    projectorGatewayRegistry.lookup(metadata),
                    factory
            );
        }

    }
}
