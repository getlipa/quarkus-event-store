package com.getlipa.eventstore.job;

import com.getlipa.eventstore.identifier.Id;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.Promise;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

@QuarkusTest
class JobRunnerTest {

    private static final Id JOB_ID = Id.numeric(0);

    private static final Id CAUSATION_ID = Id.numeric(1);

    @Inject
    JobRunner jobRunner;

    @Test
    @Order(1)
    void run() throws ExecutionException, InterruptedException {
        final var increment = new AtomicLong();
        final var result = Promise.promise();
        final var completed = jobRunner.create(JOB_ID)
                .causationId(CAUSATION_ID)
                .checkpointIntervalMs(2)
                .currentCheckpoint(7)
                .checkpointFrom(increment::getAndIncrement)
                .reportTo("domain", Id.numeric(2))
                .run(job -> result.future());
    }

}