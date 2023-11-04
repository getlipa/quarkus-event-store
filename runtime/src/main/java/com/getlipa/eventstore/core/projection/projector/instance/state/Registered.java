package com.getlipa.eventstore.core.projection.projector.instance.state;

import com.getlipa.eventstore.core.event.AnyEvent;
import com.getlipa.eventstore.core.projection.projector.Projector;
import com.getlipa.eventstore.core.projection.projector.commands.ProjectRequest;
import com.getlipa.eventstore.core.projection.projector.commands.ProjectResponse;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class Registered extends ProjectorState {

    private final Projector.Id projectorId;

    @Override
    public Future<ProjectResponse> process(ProjectRequest projectRequest) {
        log.trace("Dispatching to projector '{}': {}", projectorId, projectRequest);
        return projectRequest.getVertx().eventBus()
                .<AnyEvent>request(projectorId.toString(), projectRequest.getEvent(), Projector.DELIVERY_OPTIONS)
                .map(Message::body)
                .map(ProjectResponse::create)
                .onSuccess(reply -> log.trace("Projector replied: {}", reply))
                .recover(error -> {
                    log.warn("Message delivery to projector '{}' failed: {}", projectorId, error.toString());
                    return projectRequest.proceed(ProjectorState.unregistered());
                });
    }
}
