package com.getlipa.eventstore.projection.projector.deployment.state;

import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.projection.projector.commands.ApplyRequest;
import com.getlipa.eventstore.projection.projector.commands.ApplyResponse;
import com.getlipa.eventstore.projection.projector.deployment.DeploymentId;
import com.getlipa.eventstore.projection.projector.deployment.ProjectorDeployment;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class Registered extends ProjectorState {

    private final DeploymentId deploymentId;

    @Override
    public Future<ApplyResponse> process(ApplyRequest applyRequest) {
        log.trace("Dispatching to projector '{}': {}", deploymentId, applyRequest);
        return applyRequest.getVertx().eventBus()
                .<AnyEvent>request(deploymentId.toString(), applyRequest.getEvent(), ProjectorDeployment.DELIVERY_OPTIONS)
                .map(Message::body)
                .map(ApplyResponse::create)
                //.onSuccess(reply -> log.trace("Projector replied: {}", reply))
                .recover(error -> {
                    log.warn("Message delivery to projector '{}' failed: {}", deploymentId, error.toString());
                    return applyRequest.proceed(unregistered());
                });
    }
}
