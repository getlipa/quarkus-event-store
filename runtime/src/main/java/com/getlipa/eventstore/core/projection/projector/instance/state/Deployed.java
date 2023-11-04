package com.getlipa.eventstore.core.projection.projector.instance.state;

import com.getlipa.eventstore.core.projection.projector.Projector;
import com.getlipa.eventstore.core.projection.projector.commands.ProjectRequest;
import com.getlipa.eventstore.core.projection.projector.commands.ProjectResponse;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class Deployed extends ProjectorState {

    private final String deploymentId;

    @Override
    public Future<ProjectResponse> process(ProjectRequest projectRequest) {
        final var projectorId = Projector.Id.create(projectRequest.getTargetId(), deploymentId);
        log.trace("Projector successfully deployed: {}", projectorId);
        return projectRequest.getInstanceManager().register(projectorId)
                .map(vd -> ProjectorState.registered(projectorId))
                .otherwise(cause -> ProjectorState.registrationFailed(projectorId, deploymentId, cause))
                .flatMap(projectRequest::proceed);
    }
}
