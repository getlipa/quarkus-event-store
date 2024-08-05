package com.getlipa.eventstore.mutex;

import com.getlipa.eventstore.aggregate.Log;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Accessors(fluent = true)
@RequiredArgsConstructor
public class LockController {

    private final Future<Lock> lock;

    static LockController create(Log<LockState> log) {
        return new LockController(Lock.acquire(log));
    }

    public Future<Lock> get() {
        return lock;
    }

    public <T> Future<T> thenRun(Runnable<T> runnable) {
        return lock.flatMap(lock -> runnable.run(lock.getInstanceId()))
                .andThen(result -> release());
    }

    void release() {
        lock.onSuccess(lock -> lock.release()
                .onSuccess(vd -> log.trace("Lock released: {}", lock))
                .onFailure(failure -> log.error("Unable to release lock: {} - {}", lock, failure.getMessage())));
    }

    public interface Runnable<T> {

        Future<T> run(long instanceId);
    }
}
