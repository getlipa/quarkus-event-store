package com.getlipa.eventstore.core.projection.projector.instance;

import com.getlipa.eventstore.core.projection.projector.Projector;
import com.getlipa.eventstore.core.projection.trgt.ProjectionTarget;
import io.vertx.core.Future;

public interface InstanceManager {

    Future<Projector.Id> resolve(final ProjectionTarget.Id targetId);

    Future<Void> register(final Projector.Id projectorId);

    Future<Void> unregister(final Projector.Id projectorId);
}
