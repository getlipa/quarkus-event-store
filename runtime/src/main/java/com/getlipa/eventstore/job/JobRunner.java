package com.getlipa.eventstore.job;

import com.getlipa.eventstore.EventStore;
import com.getlipa.eventstore.identifier.Id;
import io.quarkus.scheduler.Scheduler;
import io.vertx.core.Future;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.*;

@ApplicationScoped
@RequiredArgsConstructor
public final class JobRunner {

    private final Scheduler scheduler;

    private final EventStore eventStore;

    public JobConfig create() {
        return create(Id.random());
    }

    public JobConfig create(Id id) {
        return JobConfig.create(id, scheduler, eventStore);
    }

    public interface Runnable<T> {

        Future<T> run(Job job);
    }
}
