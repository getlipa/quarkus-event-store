package com.getlipa.eventstore.projection.projector;

import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.projection.projector.commands.ApplyRequest;
import com.getlipa.eventstore.projection.projector.commands.ApplyResponse;
import com.getlipa.eventstore.projection.projector.deployment.DeploymentManager;
import com.getlipa.eventstore.projection.ProjectionMetadata;
import com.getlipa.eventstore.projection.projector.deployment.state.ProjectorState;
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
public class Projector {

    private final ProjectionMetadata metadata;

    private final Vertx vertx;

    private final DeploymentManager deploymentManager;

    public Future<ApplyResponse> project(AnyEvent event) {
        final var aggregateId = metadata.getContext().extractAggregateId(event);
        final var projectorId = ProjectorId.create(metadata.getName(), aggregateId);
        final var projectRequest = ApplyRequest.create(
                ProjectorId.create(metadata.getName(), aggregateId),
                vertx,
                deploymentManager,
                event
        );
        return deploymentManager.resolve(projectorId)
                .flatMap(instanceId -> projectRequest.proceed(ProjectorState.from(instanceId)));
    }

    @Unremovable
    @ApplicationScoped
    @RequiredArgsConstructor
    public static class Factory {

        private final Vertx vertx;

        private final DeploymentManager deploymentManager;

        public Projector create(final ProjectionMetadata metadata) {
            return new Projector(
                    metadata,
                    vertx,
                    deploymentManager
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