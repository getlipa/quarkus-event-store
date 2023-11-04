package com.getlipa.eventstore.core.projection.projector.instance.state;

import com.getlipa.eventstore.core.projection.projector.Projector;
import com.getlipa.eventstore.core.projection.projector.commands.ProjectRequest;
import com.getlipa.eventstore.core.projection.projector.commands.ProjectResponse;
import io.vertx.core.Future;

public abstract class ProjectorState {

    public static ProjectorState from(Projector.Id projectorId) {
        if (projectorId == null || projectorId.getInstanceId() == 0) {
            return unregistered();
        }
        return ProjectorState.registered(projectorId);
    }

    abstract public Future<ProjectResponse> process(ProjectRequest projectRequest);

    public static ProjectorState unregistered() {
        return new Unregistered();
    }

    public static ProjectorState deployed(final String deploymentId) {
        return new Deployed(deploymentId);
    }

    public static ProjectorState deploymentFailed(final Throwable failure) {
        return new DeploymentFailed(failure);
    }

    public static ProjectorState registered(final Projector.Id projectorId) {
        return new Registered(projectorId);
    }

    public static ProjectorState registrationFailed(final Projector.Id projectorId, final String deploymentId, Throwable cause) {
        return new RegistrationFailed(projectorId, deploymentId, cause);
    }
}
