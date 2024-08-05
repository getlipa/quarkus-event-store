package com.getlipa.eventstore.job;

import com.getlipa.eventstore.EventStore;
import com.getlipa.eventstore.identifier.Id;
import com.getlipa.eventstore.event.Events;
import com.getlipa.eventstore.event.selector.ByLogSelector;
import io.quarkus.scheduler.Scheduler;
import io.vertx.core.Future;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.function.Supplier;

@Getter(AccessLevel.PACKAGE)
@Accessors(fluent = true)
@RequiredArgsConstructor
public final class JobConfig {

    private final Id id;

    @Setter
    private Supplier<Long> checkpointFrom = () -> 0L;

    @Setter
    private int checkpointIntervalMs = 1000; // FIXME: Use config property!!;

    @Setter
    private ByLogSelector reportTo = Events.byLog("$job", Id.random());

    @Setter
    private Id causationId = Id.random();

    private final Scheduler scheduler;

    private final EventStore eventStore;

    static JobConfig create(Id id, Scheduler scheduler, EventStore eventStore) {
        return new JobConfig(id, scheduler, eventStore);
    }

    public JobConfig currentCheckpoint(long checkpoint) {
        return checkpointFrom(() -> checkpoint);
    }

    public JobConfig reportTo(String domain, String id) {
        return reportTo(domain, Id.derive(id));
    }

    public JobConfig reportTo(String domain, Id id) {
        return reportTo(Events.byLog(domain, id));
    }

    public <T> Future<Job> start(JobRunner.Runnable<T> runnable) {
        final var jobExecutor = Job.create(this, scheduler, eventStore.stream(reportTo));
        return jobExecutor.start(runnable)
                .map(jobExecutor);
    }

    public <T> Future<Long> run(JobRunner.Runnable<T> runnable) {
        return start(runnable)
                .flatMap(Job::completed);
    }
}
