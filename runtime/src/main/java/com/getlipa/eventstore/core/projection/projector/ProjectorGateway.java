package com.getlipa.eventstore.core.projection.projector;

import com.getlipa.eventstore.core.event.AnyEvent;
import com.getlipa.eventstore.core.projection.ProjectionMetadata;
import com.getlipa.eventstore.core.projection.projector.instance.InstanceManager;
import com.getlipa.eventstore.core.projection.projector.commands.ProjectRequest;
import com.getlipa.eventstore.core.projection.projector.commands.ProjectResponse;
import com.getlipa.eventstore.core.projection.projector.instance.state.ProjectorState;
import com.getlipa.eventstore.core.projection.trgt.ProjectionTarget;
import io.quarkus.arc.SyntheticCreationalContext;
import io.quarkus.arc.Unremovable;
import io.quarkus.runtime.annotations.Recorder;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class ProjectorGateway {

    private final ProjectionMetadata metadata;

    private final Vertx vertx;

    private final InstanceManager instanceManager;

    public Future<ProjectResponse> deliver(AnyEvent event) {
        final var id = metadata.getDispatchStrategy().determineTargetId(event);
        final var targetId = ProjectionTarget.Id.create(metadata.getName(), id);
        final var projectRequest = ProjectRequest.create(targetId, vertx, instanceManager, event);
        return instanceManager.resolve(targetId)
                .flatMap(instanceId -> projectRequest.proceed(ProjectorState.from(instanceId)));
        // FIXME: Where to decide that events need to be parked? Projector? -> Gateway only cares about delivery!!
    }

    @Unremovable
    @ApplicationScoped
    @RequiredArgsConstructor
    public static class Factory {

        private final Vertx vertx;

        private final InstanceManager instanceManager;

        public ProjectorGateway create(final ProjectionMetadata metadata) {
            return new ProjectorGateway(
                    metadata,
                    vertx,
                    instanceManager
            );
        }
    }

    @Slf4j
    @Recorder
    public static class BeanRecorder {

        public Function<SyntheticCreationalContext<Object>, Object> record(
                final ProjectionMetadata metadata
        ) {
            return context -> CDI.current()
                    .select(Factory.class)
                    .get()
                    .create(metadata);
        }
    }
}