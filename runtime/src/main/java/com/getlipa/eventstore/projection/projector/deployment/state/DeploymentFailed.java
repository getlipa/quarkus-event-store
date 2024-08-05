package com.getlipa.eventstore.projection.projector.deployment.state;

import com.getlipa.eventstore.projection.projector.commands.ApplyRequest;
import com.getlipa.eventstore.projection.projector.commands.ApplyResponse;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DeploymentFailed extends ProjectorState {

    private final Throwable cause;

    @Override
    public Future<ApplyResponse> process(ApplyRequest applyRequest) {
        log.error("Unable to deploy projector: {} - {}", applyRequest.getProjectorId(), cause.toString());
        return Future.failedFuture(cause);
    }
}
