package com.getlipa.eventstore.mutex;

import com.getlipa.eventstore.Mutex;
import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.event.EventMetadata;
import com.getlipa.eventstore.aggregate.Log;
import io.vertx.core.Future;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class Lock {

    @Getter
    private final long instanceId;

    private final Log<LockState> lockStateLog;

    static Future<Lock> acquire(final Log<LockState> lockState) {
        final var id = lockState.getId();
        log.trace("Trying to acquire lock: {}", id);
        return tryAppendAcquired(lockState)
                .map(EventMetadata::getLogIndex)
                .recover(error -> Future.failedFuture(String.format(
                        "Cannot acquire lock: %s - %s",
                        id,
                        error.getMessage())
                ))
                .map(index -> new Lock(index, lockState))
                .onSuccess(lock -> log.trace("Lock acquired: {}", lock));
    }

    static Future<AnyEvent> tryAppendAcquired(Log<LockState> log) {
        return log.get()
                .flatMap(lockState -> {
                    if (lockState.isLocked()) {
                        return Future.failedFuture("Already locked.");
                    }
                    return log.append()
                            .withPayload(Mutex.LockAcquired.newBuilder().build());
                });
    }

    Future<Long> release() {
        return tryAppendReleased()
                .map(EventMetadata::getLogIndex);
    }

    Future<AnyEvent> tryAppendReleased() {
        return lockStateLog.get()
                .flatMap(lockState -> {
                    if (!lockState.isLocked()) {
                        return Future.failedFuture("Lock has not been acquired.");
                    }
                    return lockStateLog.append()
                            .withPayload(Mutex.LockReleased.newBuilder().build());
                });

    }

    @Override
    public String toString() {
        return String.format("%s #%d", lockStateLog.toString(), instanceId);
    }
}
