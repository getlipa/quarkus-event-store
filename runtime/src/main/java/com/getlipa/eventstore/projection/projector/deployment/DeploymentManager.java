package com.getlipa.eventstore.projection.projector.deployment;

import com.getlipa.eventstore.projection.projector.ProjectorId;
import io.vertx.core.Future;

public interface DeploymentManager {

    Future<DeploymentId> resolve(final ProjectorId projectorId);

    Future<Void> register(final DeploymentId deploymentId);

    Future<Void> unregister(final DeploymentId deploymentId);
}
