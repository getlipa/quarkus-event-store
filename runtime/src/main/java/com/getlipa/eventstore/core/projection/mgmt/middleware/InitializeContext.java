package com.getlipa.eventstore.core.projection.mgmt.middleware;

import com.getlipa.eventstore.core.projection.mgmt.ProjectionManager;
import io.vertx.core.Future;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class InitializeContext {

    private final ProjectionManager next;

    public static InitializeContext create(final ProjectionManager manager) {
        return new InitializeContext(manager);
    }

    public Future<Void> proceed() {
        return next.initialize();
    }
}
