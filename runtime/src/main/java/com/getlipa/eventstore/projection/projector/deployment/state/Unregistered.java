package com.getlipa.eventstore.projection.projector.deployment.state;

import com.getlipa.eventstore.projection.projector.commands.ApplyRequest;
import com.getlipa.eventstore.projection.projector.commands.ApplyResponse;
import com.getlipa.eventstore.projection.projector.ProjectorFactory;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class Unregistered extends ProjectorState {

    @Override
    public Future<ApplyResponse> process(final ApplyRequest applyRequest) {
        final var targetId = applyRequest.getProjectorId();
        log.trace("No projector registration found: {}", targetId);
        return applyRequest.getVertx()
                .deployVerticle(String.format("%s:%s", ProjectorFactory.PREFIX, targetId.toVerticleId()))
                .map(ProjectorState::deployed)
                .otherwise(ProjectorState::deploymentFailed)
                .flatMap(applyRequest::proceed);
    }
}
