package com.getlipa.eventstore.core.projection.projector.instance.state;

import com.getlipa.eventstore.core.projection.projector.Projector;
import com.getlipa.eventstore.core.projection.projector.commands.ProjectRequest;
import com.getlipa.eventstore.core.projection.projector.commands.ProjectResponse;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class RegistrationFailed extends ProjectorState {

    private final Projector.Id projectorId;

    private final String deploymentId;

    private final Throwable cause;

    @Override
    public Future<ProjectResponse> process(ProjectRequest projectRequest) {
        log.trace("Unable to register projector: {} - {}", projectorId, cause.toString());
        projectRequest.getVertx().undeploy(deploymentId)
                .onSuccess(vd -> log.trace("Projector deployment reverted: {}", projectRequest))
                .onFailure(error -> {
                    log.error("Unable to revert deployment: {} - {}", projectorId, error.toString());
                    log.warn(
                            "Projector may be active but not receiving messages until it has undeployed itself: {}",
                            projectorId
                    );
                });
        return projectRequest.getInstanceManager().resolve(projectRequest.getTargetId())
                .flatMap(projectorId -> projectRequest.proceed(ProjectorState.from(projectorId)));
    }
}
