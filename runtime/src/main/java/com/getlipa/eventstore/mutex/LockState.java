package com.getlipa.eventstore.mutex;

import com.getlipa.eventstore.Mutex;
import com.getlipa.eventstore.aggregate.middleware.Use;
import com.getlipa.eventstore.event.Event;
import com.getlipa.eventstore.projection.cdi.Projection;
import com.getlipa.eventstore.hydration.eventhandler.Apply;
import com.getlipa.eventstore.aggregate.hydration.ReHydrateMiddleware;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Projection(name = "$lock", context = "$lock")
@Getter
@Use(ReHydrateMiddleware.class)
public class LockState {

    private boolean isLocked = false;

    @Apply
    public void onAcquired(Event<Mutex.LockAcquired> acquiredEvent) {
        isLocked = true;
    }

    @Apply
    public void onReleased(Event<Mutex.LockReleased> releasedEvent) {
        isLocked = false;
    }
}
