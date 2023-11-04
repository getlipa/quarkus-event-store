package com.getlipa.eventstore.core.projection.mgmt;

import com.getlipa.eventstore.core.EventStore;
import com.getlipa.eventstore.core.projection.projector.ProjectorGateway;
import com.getlipa.eventstore.core.projection.ProjectionMetadata;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ProjectionManager {

    private final ProjectionMetadata metadata;

    private final EventStore eventStore;

    private final ProjectorGateway gateway;

    public Future<Void> initialize() {
        return Future.succeededFuture();
    }

    public Future<Void> cleanup() {
        return Future.succeededFuture();
    }

}
