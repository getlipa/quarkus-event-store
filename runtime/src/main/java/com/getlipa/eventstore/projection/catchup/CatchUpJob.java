package com.getlipa.eventstore.projection.catchup;

import com.getlipa.eventstore.EventStore;
import com.getlipa.eventstore.identifier.Id;
import com.getlipa.eventstore.projection.Companion;
import com.getlipa.eventstore.projection.ProjectionMetadata;
import com.getlipa.eventstore.projection.projector.Projector;
import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.job.JobRunner;
import com.getlipa.eventstore.mutex.Mutex;
import com.getlipa.eventstore.aggregate.Aggregate;
import com.getlipa.eventstore.aggregate.Aggregates;
import com.getlipa.eventstore.stream.reader.cursor.Cursor;
import io.vertx.core.Future;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CatchUpJob implements AnyEvent.Handler<Void> {

    public static String JOB_ID_NAMESPACE = "catch-up-job";

    public static Id JOB_CAUSATION_ID = Id.derive(JOB_ID_NAMESPACE);

    private final EventStore eventStore;

    private final ProjectionMetadata metadata;

    private final Aggregate<ProjectionProgress> state;

    private final Projector projector;

    private final JobRunner jobRunner;

    private final Mutex mutex;

    public Future<Void> start() {
        return state.get()
                .flatMap(state -> {
                    if (state.caughtUp()) {
                        log.debug("Projection has already been caught up: {}", metadata.getName());
                        return Future.succeededFuture();
                    }
                    return start(state.getCatchUpTip(), state.getCatchUpTarget());
                });
    }

    public Future<Void> start(final long from, final long until) {
        final var jobId = Id.derive(JOB_ID_NAMESPACE, metadata.getName());
        return mutex.acquireLock(jobId)
                .thenRun(lockId -> catchUp(jobId, from, until))
                .onFailure(failure -> log.trace(
                        "Could not run projection catch-up: {} - {}",
                        metadata.getName(),
                        failure.getMessage()

                ))
                .onSuccess(position -> log.info(
                        "Projection catch-up completed: {} @{}",
                        metadata.getName(),
                        position
                ))
                .mapEmpty();
    }

    Future<Long> catchUp(Id jobId, final long from, final long until) {
        log.info("Projection catch-up started: {} - {}", metadata.getName(), jobId);
        return jobRunner.create(jobId)
                .reportTo("$projection", metadata.getName())
                .checkpointIntervalMs(2000)
                .currentCheckpoint(0)
                .causationId(JOB_CAUSATION_ID)
                .run(job -> eventStore.stream(metadata.getContext().createSelector())
                        .readForward()
                        .from(Cursor.position(from))
                        .until(Cursor.position(until))
                        .forEach(event -> projector.project(event)
                                .onComplete(result -> job.checkpoint(event.getPosition()))
                        )
                );
    }

    @Override
    public Future<Void> handle(AnyEvent event) {
        return projector.project(event)
                .mapEmpty();
    }

    @ApplicationScoped
    @RequiredArgsConstructor
    public static class Factory {

        private final EventStore eventStore;

        private final Companion<Projector> projectorGatewayCompanion;

        private final Aggregates<ProjectionProgress> projectedLogs;

        private final JobRunner jobRunner;

        private final Mutex mutex;

        public CatchUpJob create(ProjectionMetadata metadata) {
            return new CatchUpJob(
                    eventStore,
                    metadata,
                    projectedLogs.get(Id.derive(metadata.getName())), // FIXME
                    projectorGatewayCompanion.lookup(metadata),
                    jobRunner,
                    mutex
            );
        }

    }
}
