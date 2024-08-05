package com.getlipa.eventstore.projection.projector.commands;

import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.projection.projector.deployment.DeploymentManager;
import com.getlipa.eventstore.projection.projector.deployment.state.ProjectorState;
import com.getlipa.eventstore.projection.projector.ProjectorId;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public class ApplyRequest {

    private final AnyEvent event;

    private final ProjectorId projectorId;

    private final Vertx vertx;

    private final DeploymentManager deploymentManager;

    private final Map<Class<? extends ProjectorState>, Integer> countsPerState = new HashMap<>();

    public static ApplyRequest create(
            final ProjectorId id,
            final Vertx vertx,
            final DeploymentManager deploymentManager,
            final AnyEvent event
    ) {
        return new ApplyRequest(event, id, vertx, deploymentManager);
    }

    public Future<ApplyResponse> proceed(ProjectorState projectorState) {
        final var count = countsPerState.merge(projectorState.getClass(), 1, Integer::sum);
        if (count > 1) {
            return Future.failedFuture(String.format(
                    "Re-transition prevented: %s",
                    projectorState.getClass().getSimpleName()
            ));
        }
        return projectorState.process(this);
    }
}
