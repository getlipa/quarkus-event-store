package com.getlipa.eventstore.core.projection.projected;

import com.getlipa.eventstore.core.Registry;
import com.getlipa.eventstore.core.projection.ProjectionMetadata;
import com.getlipa.eventstore.core.projection.trgt.ProjectionTarget;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class ProjectedProducer {

    private final ProjectedStream.Factory projectedStreamFactory;

    private final ProjectedLog.Factory projectedLogFactory;

    private final Registry<ProjectionMetadata> projectionMetadataRegistry;

    @Produces
    @Dependent
    <T> ProjectedLog<T> produceProjectedLog(ProjectionTarget.Id id) {
        return projectedLogFactory.create(id.getType(), id.getName());
    }

    @Produces
    @Dependent
    <T> ProjectedLogs<T> produceProjectedLogs(InjectionPoint injectionPoint) {
        final var metadata = projectionMetadataRegistry.lookup(injectionPoint);
        projectedLogFactory.ensureCompatibility(metadata);
        return new ProjectedLogs<>(
                metadata,
                projectedLogFactory
        );
    }

    @Produces
    @Dependent
    <T> ProjectedStream<T> produceProjectedStream(ProjectionTarget.Id id) {
        return projectedStreamFactory.create(id.getType(), id.getName());
    }

    @Produces
    @Dependent
    <T> ProjectedStreams<T> produceProjectedStreams(InjectionPoint injectionPoint) {
        return new ProjectedStreams<>(
                projectionMetadataRegistry.lookup(injectionPoint),
                projectedStreamFactory
        );
    }
}
