package com.getlipa.eventstore.core.projection.projector.instance.state;

import com.getlipa.eventstore.core.projection.projector.commands.ProjectRequest;
import com.getlipa.eventstore.core.projection.projector.commands.ProjectResponse;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DeploymentFailed extends ProjectorState {

    private final Throwable cause;

    @Override
    public Future<ProjectResponse> process(ProjectRequest projectRequest) {
        log.error("Unable to deploy projector: {} - {}", projectRequest.getTargetId(), cause.toString());
        return Future.failedFuture(cause);
    }
}
