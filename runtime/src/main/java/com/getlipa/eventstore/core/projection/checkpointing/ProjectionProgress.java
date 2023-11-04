package com.getlipa.eventstore.core.projection.checkpointing;

import com.getlipa.eventstore.core.event.AnyEvent;
import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.projection.cdi.Events;
import com.getlipa.eventstore.core.projection.cdi.Projection;
import com.getlipa.eventstore.core.projection.projector.DispatchStrategy;
import com.getlipa.eventstore.core.projection.trgt.eventhandler.Project;
import com.getlipa.eventstore.core.projection.trgt.middleware.Apply;
import com.getlipa.eventstore.core.projection.trgt.middleware.stateful.CatchUpMiddleware;
import com.getlipa.eventstore.subscriptions.Projections;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@Projection(name = "$projection-progress")
@Events.WithLogDomain("$projection-progress")
@Events.Dispatch(DispatchStrategy.BY_LOG_ID)
@Apply(CatchUpMiddleware.class)
public class ProjectionProgress {

    private final Map<UUID, Checkpoint> checkpoints = new HashMap<>();

    @Getter
    private long catchUpTarget = Long.MAX_VALUE;

    @Getter
    private long catchUpTip = 0;

    @Getter
    private UUID catchUpProcessId;

    private int expectedCheckpointIndex;

    public boolean catchingUp() {
        return catchUpProcessId != null;
    }

    public boolean caughtUp() {
        return catchUpTip >= catchUpTarget;
    }

    @Project
    public void onSubscriptionRegistered(Event<Projections.ListeningStarted> event) {
        checkpoints.put(extractCheckpointProcessId(event), Checkpoint.initial(event, expectedCheckpointIndex));
    }

    @Project
    public void onSubscriptionDeregistered(Event<Projections.ListeningStopped> event) {
        checkpoints.remove(extractCheckpointProcessId(event));
    }

    @Project
    public void oCatchUpStarted(Event<Projections.CatchUpStarted> event) {
        catchUpProcessId = extractCheckpointProcessId(event);
    }

    @Project
    public void oCatchUpStopped(Event<Projections.CatchUpCompleted> event) {
        catchUpProcessId = null;
        catchUpTip = event.getPayload().get().getTip();
    }

    @Project
    public void onCheckpointReached(Event<Projections.CheckpointReached> event) {
        final var checkpoint = checkpoints.get(extractCheckpointProcessId(event));
        if (checkpoint == null) {
            log.warn(
                    "Ignored checkpoint reported by stale checkpoint process: {} / {}",
                    extractCheckpointProcessId(event),
                    event
            );
            return;
        }
        checkpoint.update(expectedCheckpointIndex, event);
        expectedCheckpointIndex = Integer.max(expectedCheckpointIndex, checkpoint.index);
        if (isCausedByCatchUp(event)) {
            catchUpTip = checkpoint.tip;
        }
        checkpoints.values().removeIf(otherCheckpoint -> {
            if (expectedCheckpointIndex - otherCheckpoint.index <= 1) {
                return false;
            }
            catchUpTarget = Long.max(catchUpTarget, otherCheckpoint.startedAt);
            log.warn(
                    "Outdated checkpoint detected - catch up required from {} until {}",
                    catchUpTip,
                    catchUpTarget
            );
            return true;
        });
    }

    UUID extractCheckpointProcessId(AnyEvent event) {
        return event.getCausationId();
    }

    boolean isCausedByCatchUp(AnyEvent event) {
        if (event == null) {
            return false;
        }
        return extractCheckpointProcessId(event).equals(catchUpProcessId);
    }

    public long tip() {
        if (checkpoints.isEmpty()) {
            return catchUpTip;
        }
        return Collections.max(checkpoints.values()).tip;
    }

    @Getter
    @AllArgsConstructor
    static class Checkpoint implements Comparable<Checkpoint> {

        private final long startedAt;

        private int index;

        private long tip;

        static Checkpoint initial(AnyEvent event, int index) {
            return new Checkpoint(event.getPosition(), index, event.getPosition());
        }

        private void update(int expectedCheckpointIndex, Event<Projections.CheckpointReached> event) {
            index = Integer.max(expectedCheckpointIndex, index + 1);
            tip = Long.min(event.getPayload().get().getTip(), event.getPosition());
        }

        @Override
        public int compareTo(Checkpoint other) {
            return Long.compare(tip, other.tip);
        }
    }
}
