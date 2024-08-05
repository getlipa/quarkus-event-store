package com.getlipa.eventstore.projection.projector.deployment.state;

import com.getlipa.eventstore.projection.projector.commands.ApplyRequest;
import com.getlipa.eventstore.projection.projector.commands.ApplyResponse;
import com.getlipa.eventstore.projection.projector.deployment.DeploymentId;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class RegistrationFailed extends ProjectorState {

    private final DeploymentId deploymentId;

    private final String verticleId;

    private final Throwable cause;

    @Override
    public Future<ApplyResponse> process(ApplyRequest applyRequest) {
        log.trace("Unable to register projector: {} - {}", deploymentId, cause.toString());
        applyRequest.getVertx().undeploy(verticleId)
                .onSuccess(vd -> log.trace("Projector deployment reverted: {}", applyRequest))
                .onFailure(error -> {
                    log.error("Unable to revert deployment: {} - {}", deploymentId, error.toString());
                    log.warn(
                            "Projector may be active but not receiving messages until it has undeployed itself: {}",
                            deploymentId
                    );
                });
        return applyRequest.getDeploymentManager().resolve(applyRequest.getProjectorId())
                .flatMap(projectorId -> applyRequest.proceed(ProjectorState.from(projectorId)));
    }
}
