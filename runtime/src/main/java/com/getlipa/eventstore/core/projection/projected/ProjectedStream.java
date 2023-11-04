package com.getlipa.eventstore.core.projection.projected;

import com.getlipa.eventstore.core.Registry;
import com.getlipa.eventstore.core.event.EventMetadata;
import com.getlipa.eventstore.core.projection.trgt.ProjectionTarget;
import com.getlipa.eventstore.core.projection.trgt.ProjectionTargetFactory;
import io.vertx.core.Future;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class ProjectedStream<T> {

    private static final long EMPTY_STREAM_REVISION = -1;

    protected final ProjectionTarget<T> projectionTarget;

    private Future<T> initFuture;

    public static <T> ProjectedStream<T> create(ProjectionTarget<T> projectionTarget) {

        return new ProjectedStream<>(projectionTarget);
    }

    public ProjectionTarget.Id getId() {
        return projectionTarget.getId();
    }

    public Future<T> get() {
        return get(false);
    }

    public Future<T> get(final boolean refresh) {
        if (refresh) {
            return projectionTarget.refreshed();
        }
        return projectionTarget.initialized();
    }

    public long getRevision() {
        return Optional.of(projectionTarget)
                .map(ProjectionTarget::getEventTip)
                .map(this::extractRevision)
                .orElse(EMPTY_STREAM_REVISION);
    }

    protected long extractRevision(EventMetadata eventMetadata) {
        return eventMetadata.getPosition();
    }


    @ApplicationScoped
    @RequiredArgsConstructor
    public static class Factory {

        private final Registry<ProjectionTargetFactory> factoryRegistry;

        public <T> ProjectedStream<T> create(String type, String name) {
            return ProjectedStream.create(
                    factoryRegistry.lookup(type).create(name)
            );
        }
    }
}
