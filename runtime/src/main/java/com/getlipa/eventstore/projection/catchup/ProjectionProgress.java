package com.getlipa.eventstore.projection.catchup;

import com.getlipa.eventstore.Jobs;
import com.getlipa.eventstore.Mutex;
import com.getlipa.eventstore.identifier.Id;
import com.getlipa.eventstore.aggregate.middleware.Use;
import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.event.Event;
import com.getlipa.eventstore.projection.cdi.Projection;
import com.getlipa.eventstore.hydration.eventhandler.Apply;
import com.getlipa.eventstore.aggregate.hydration.ReHydrateMiddleware;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Projection(name = "$projection", context = "$projection")
@Use(ReHydrateMiddleware.class)
public class ProjectionProgress {

    private final Map<Id, JobProgress> jobProgresses = new HashMap<>();

    private static final int STALE_TOLERANCE_MS = 10000; // TODO: create config

    @Getter
    private long catchUpTarget = Long.MAX_VALUE;

    @Getter
    private long catchUpTip = 0;

    @Getter
    private Id catchUpJobId;

    public boolean catchingUp() {
        return catchUpJobId != null;
    }

    public boolean caughtUp() {
        return catchUpTip >= catchUpTarget;
    }

    @Apply
    public void onJobStarted(Event<Jobs.JobStarted> jobStarted) {
        final var jobId = jobIdFrom(jobStarted);
        if (CatchUpJob.JOB_CAUSATION_ID.equals(jobStarted.getCausationId())) {
            catchUpJobId = jobId;
        }
        jobProgresses.put(jobId, JobProgress.started(jobStarted));
        removeStaleJobs(jobStarted);
    }

    @Apply
    public void onJobCompleted(Event<Jobs.JobCompleted> jobCompleted) {
        final var jobId = jobIdFrom(jobCompleted);
        if (jobId.equals(catchUpJobId)) {
            onCatchUpCompleted(jobCompleted);
        }
        jobProgresses.remove(jobId);
    }

    @Apply
    public void onCheckpointReached(Event<Jobs.CheckpointReached> checkpointReached) {
        final var jobId = jobIdFrom(checkpointReached);
        final var checkpoint = jobProgresses.get(jobId);
        if (checkpoint == null) {
            log.warn("Received checkpoint for unknown job: {}", jobId);
            return;
        }
        checkpoint.update(checkpointReached);
    }

    @Apply
    public void onLockReleased(Event<Mutex.LockReleased> lockReleasedEvent) {
        log.error("INVALID: {}-{}", lockReleasedEvent.getLogContext(), lockReleasedEvent.getLogId());
    }

    void onCatchUpCompleted(final Event<Jobs.JobCompleted> jobCompleted) {
        catchUpTip = jobCompleted.getPayload().get().getCheckpoint();
        catchUpTarget = jobProgresses.values().stream()
                .map(JobProgress::getStartedAt)
                .min(Comparator.naturalOrder())
                .orElse(Long.MAX_VALUE);
        catchUpJobId = null;
    }

    void removeStaleJobs(final AnyEvent latestEvent) {
        final var iterator = jobProgresses.entrySet().iterator();
        while (iterator.hasNext()) {
            final var entry = iterator.next();
            final var jobProgress = entry.getValue();
            if (jobProgress.isStale(latestEvent.getCreatedAt())) {
                iterator.remove();
                catchUpTip = Long.min(catchUpTip, jobProgress.tip);
                log.warn("Stale job detected: {} (last known tip: {})", entry.getKey(), jobProgress.tip);
            }
        }
    }

    Id jobIdFrom(final AnyEvent event) {
        return event.getCorrelationId();
    }

    public long tip() {
        return jobProgresses.values().stream()
                .map(JobProgress::getTip)
                .min(Comparator.naturalOrder())
                .orElse(catchUpTip);
    }

    @Getter
    @AllArgsConstructor
    static class JobProgress {

        private final long startedAt;

        private int checkpointIntervalMs;

        private long tip;

        private OffsetDateTime updatedAt;

        static JobProgress started(final Event<Jobs.JobStarted> jobStarted) {
            return new JobProgress(
                    jobStarted.getPosition(),
                    jobStarted.get().getCheckpointIntervalMs(),
                    jobStarted.getPosition(),
                    jobStarted.getCreatedAt()
            );
        }

        void update(final Event<Jobs.CheckpointReached> checkpointReached) {
            tip = Long.min(checkpointReached.get().getCheckpoint(), checkpointReached.getPosition());
            updatedAt = checkpointReached.getCreatedAt();
        }

        boolean isStale(final OffsetDateTime now) {
            return updatedAt.plus(STALE_TOLERANCE_MS, ChronoUnit.MILLIS).isBefore(now);
        }
    }
}
