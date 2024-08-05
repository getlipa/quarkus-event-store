package com.getlipa.eventstore.projection.projector.deployment.state;

import com.getlipa.eventstore.projection.projector.commands.ApplyRequest;
import com.getlipa.eventstore.projection.projector.commands.ApplyResponse;
import com.getlipa.eventstore.projection.projector.deployment.DeploymentId;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class Deployed extends ProjectorState {

    private final String deploymentId;

    @Override
    public Future<ApplyResponse> process(ApplyRequest applyRequest) {
        final var projectorId = DeploymentId.create(applyRequest.getProjectorId(), deploymentId);
        log.trace("Projector successfully deployed: {}", projectorId);
        return applyRequest.getDeploymentManager().register(projectorId)
                .map(vd -> registered(projectorId))
                .otherwise(cause -> registrationFailed(projectorId, deploymentId, cause))
                .flatMap(applyRequest::proceed);
    }
}
