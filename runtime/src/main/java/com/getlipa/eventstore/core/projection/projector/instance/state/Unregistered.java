package com.getlipa.eventstore.core.projection.projector.instance.state;

import com.getlipa.eventstore.core.projection.projector.commands.ProjectRequest;
import com.getlipa.eventstore.core.projection.projector.ProjectorFactory;
import com.getlipa.eventstore.core.projection.projector.commands.ProjectResponse;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class Unregistered extends ProjectorState {

    @Override
    public Future<ProjectResponse> process(final ProjectRequest projectRequest) {
        final var targetId = projectRequest.getTargetId();
        log.trace("No projector registration found: {}", targetId);
        return projectRequest.getVertx().deployVerticle(String.format("%s:%s", ProjectorFactory.PREFIX, targetId))
                .map(ProjectorState::deployed)
                .otherwise(ProjectorState::deploymentFailed)
                .flatMap(projectRequest::proceed);
    }
}
