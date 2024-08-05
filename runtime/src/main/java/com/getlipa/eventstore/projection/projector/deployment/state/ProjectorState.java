package com.getlipa.eventstore.projection.projector.deployment.state;

import com.getlipa.eventstore.projection.projector.commands.ApplyRequest;
import com.getlipa.eventstore.projection.projector.commands.ApplyResponse;
import com.getlipa.eventstore.projection.projector.deployment.DeploymentId;
import io.vertx.core.Future;

public abstract class ProjectorState {

    public static ProjectorState from(DeploymentId deploymentId) {
        if (deploymentId == null || deploymentId.getInstanceId() == 0) {
            return unregistered();
        }
        return ProjectorState.registered(deploymentId);
    }

    abstract public Future<ApplyResponse> process(ApplyRequest applyRequest);

    public static ProjectorState unregistered() {
        return new Unregistered();
    }

    public static ProjectorState deployed(final String deploymentId) {
        return new Deployed(deploymentId);
    }

    public static ProjectorState deploymentFailed(final Throwable failure) {
        return new DeploymentFailed(failure);
    }

    public static ProjectorState registered(final DeploymentId deploymentId) {
        return new Registered(deploymentId);
    }

    public static ProjectorState registrationFailed(final DeploymentId instanceId, final String deploymentId, Throwable cause) {
        return new RegistrationFailed(instanceId, deploymentId, cause);
    }
}
