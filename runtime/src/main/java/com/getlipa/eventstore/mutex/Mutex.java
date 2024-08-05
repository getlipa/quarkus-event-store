package com.getlipa.eventstore.mutex;

import com.getlipa.eventstore.identifier.Id;
import com.getlipa.eventstore.aggregate.Logs;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class Mutex {

    private final Logs<LockState> logs;

    public LockController acquireLock(Id id) {
        return LockController.create(logs.get(id));
    }

}
