package com.getlipa.eventstore.job;

import com.getlipa.eventstore.Jobs;
import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.event.EventMetadata;
import com.getlipa.eventstore.event.logindex.LogIndex;
import com.getlipa.eventstore.stream.AppendableStream;
import com.google.protobuf.Message;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.Scheduler;
import io.smallrye.mutiny.Uni;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@RequiredArgsConstructor
@Slf4j
public final class Job {

    private final JobConfig jobConfig;

    private final Scheduler scheduler;

    private final AppendableStream appendableStream;

    private Future<Long> completed;

    private Supplier<Long> checkpointSupplier = () -> 0L;

    static Job create(
            final JobConfig jobConfig,
            final Scheduler scheduler,
            final AppendableStream appendableStream
    ) {
        return new Job(jobConfig, scheduler, appendableStream);
    }

    public void checkpoint(Supplier<Long> checkpointSupplier) {
        this.checkpointSupplier = checkpointSupplier;
    }

    public void checkpoint(long checkpoint) {
        checkpoint(() -> checkpoint);
    }

    public Future<Long> completed() {
        if (completed == null) {
            throw new IllegalStateException("Job has not been started");
        }
        return completed;
    }

    Future<Long> complete() {
        unscheduleCheckpointing();
        return append(Jobs.JobCompleted.newBuilder()
                .setCheckpoint(checkpointSupplier.get())
                .build())
                .map(EventMetadata::getPosition);
    }

    <T> Future<Long> start(JobRunner.Runnable<T> runnable) {
        if (completed != null) {
            throw new IllegalStateException("Job already started");
        }
        final var started = appendStarted()
                .andThen(this::scheduleCheckpointing)
                .map(AnyEvent::getPosition)
                .onSuccess(position -> log.trace("Job started: {} @{}", jobConfig.id(), position))
                .onFailure(failure -> log.trace("Could not start job: {} - {}", jobConfig.id(), failure.getMessage()));
        completed = started.flatMap(position -> runnable.run(this).map(position))
                .flatMap(position -> complete());
        return started;
    }

    <P extends Message> Future<AnyEvent> append(P payload) {
        return append().withPayload(payload);
    }

    Future<Void> persistCheckpoint() {
        final var checkpoint = checkpointSupplier.get();
        return appendCheckpointReached(checkpoint)
                .andThen(vd -> log.trace(
                        "Checkpoint persisted for job: {} - {} / {}",
                        jobConfig.id(),
                        appendableStream.toString(),
                        checkpoint
                ))
                .onFailure(error -> log.warn(
                        "Failed to persist checkpoint {} for job: {} - {}",
                        checkpoint,
                        jobConfig.id(),
                        error.getMessage())
                )
                .mapEmpty();
    }

    Future<AnyEvent> appendCheckpointReached(final long checkpoint) {
        return append(Jobs.CheckpointReached.newBuilder()
                .setCheckpoint(checkpoint)
                .build());
    }

    Future<AnyEvent> appendStarted() {
        return append(Jobs.JobStarted.newBuilder()
                .setCheckpoint(jobConfig.checkpointFrom().get())
                .setCheckpointIntervalMs(jobConfig.checkpointIntervalMs())
                .build());
    }

    AppendableStream.Appender append() {
        return appendableStream.append(LogIndex.atAny())
                .withCorrelationId(jobConfig.id())
                .withCausationId(jobConfig.causationId());
    }

    void scheduleCheckpointing(AsyncResult<AnyEvent> result) {
        if (result.failed()) {
            return;
        }
        final var interval = String.format("%.3fs", jobConfig.checkpointIntervalMs() / 1000d);
        final var trigger = scheduler.newJob(jobConfig.id().shortUUID())
                .setInterval(interval)
                .setConcurrentExecution(Scheduled.ConcurrentExecution.SKIP)
                .setAsyncTask(execution -> Uni.createFrom().completionStage(persistCheckpoint().toCompletionStage()))
                .schedule();
        log.trace("Checkpointing scheduled for job: {} - {}", trigger.getId(), interval);
    }

    void unscheduleCheckpointing() {
        final var trigger = scheduler.unscheduleJob(jobConfig.id().shortUUID());
        if (trigger != null) {
            log.trace("Checkpointing unscheduled for job: {}", trigger.getId());
        }
    }
}
