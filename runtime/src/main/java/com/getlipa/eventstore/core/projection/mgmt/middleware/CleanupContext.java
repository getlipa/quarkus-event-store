package com.getlipa.eventstore.core.projection.mgmt.middleware;

import com.getlipa.eventstore.core.projection.mgmt.ProjectionManager;
import io.vertx.core.Future;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CleanupContext {

    private final ProjectionManager next;

    public static CleanupContext create(final ProjectionManager manager) {
        return new CleanupContext(manager);
    }

    public Future<Void> proceed() {
        return next.cleanup();
    }
}
