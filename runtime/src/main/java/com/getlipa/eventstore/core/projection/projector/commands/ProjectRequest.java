package com.getlipa.eventstore.core.projection.projector.commands;

import com.getlipa.eventstore.core.event.AnyEvent;
import com.getlipa.eventstore.core.projection.projector.instance.InstanceManager;
import com.getlipa.eventstore.core.projection.projector.instance.state.ProjectorState;
import com.getlipa.eventstore.core.projection.trgt.ProjectionTarget;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public class ProjectRequest {

    private final AnyEvent event;

    private final ProjectionTarget.Id targetId;

    private final Vertx vertx;

    private final InstanceManager instanceManager;

    private final Map<Class<? extends ProjectorState>, Integer> countsPerState = new HashMap<>();

    public static ProjectRequest create(
            final ProjectionTarget.Id id,
            final Vertx vertx,
            final InstanceManager instanceManager,
            final AnyEvent event
    ) {
        return new ProjectRequest(event, id, vertx, instanceManager);
    }

    public Future<ProjectResponse> proceed(ProjectorState projectorState) {
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
