package com.getlipa.eventstore.core.projection.projected;

import com.getlipa.eventstore.core.EventStore;
import com.getlipa.eventstore.core.Registry;
import com.getlipa.eventstore.core.UuidGenerator;
import com.getlipa.eventstore.core.event.EphemeralEvent;
import com.getlipa.eventstore.core.event.EventMetadata;
import com.getlipa.eventstore.core.event.Events;
import com.getlipa.eventstore.core.event.logindex.LogIndex;
import com.getlipa.eventstore.core.event.selector.ByLogSelector;
import com.getlipa.eventstore.core.projection.ProjectionMetadata;
import com.getlipa.eventstore.core.projection.trgt.ProjectionTarget;
import com.getlipa.eventstore.core.projection.trgt.ProjectionTargetFactory;
import com.getlipa.eventstore.core.stream.AppendableStream;
import com.google.protobuf.Message;
import io.vertx.core.Future;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

public class ProjectedLog<T> extends ProjectedStream<T>{

    private final AppendableStream appendableStream;

    public ProjectedLog(final ProjectionTarget<T> projectionTarget, final AppendableStream appendableStream) {
        super(projectionTarget);
        this.appendableStream = appendableStream;
    }

    public static <T> ProjectedLog<T> create(
            final ProjectionTarget<T> projectionTarget,
            final AppendableStream appendableStream
    ) {
        return new ProjectedLog<>(projectionTarget, appendableStream);
    }

    public <P extends Message> Future<T> append(EphemeralEvent<P> event) {
        final var appendAt = getRevision() + 1;
        return projectionTarget.initialized()
                .flatMap(target -> appendableStream.append(LogIndex.at(appendAt), event))
                .flatMap(projectionTarget::apply);
    }

    // FXIME: naming
    public <P extends Message> Future<T> appendAny(EphemeralEvent<P> event) {
        return projectionTarget.initialized()
                .flatMap(target -> appendableStream.append(LogIndex.atAny(), event))
                .flatMap(projectionTarget::apply);
    }

    @Override
    protected long extractRevision(EventMetadata eventMetadata) {
        return eventMetadata.getLogIndex();
    }

    @ApplicationScoped
    @RequiredArgsConstructor
    public static class Factory {

        private final Registry<ProjectionTargetFactory> projectionTargetFactories;

        private final Registry<ProjectionMetadata> metadataRegistry;

        private final EventStore eventStore;

        public void ensureCompatibility(ProjectionMetadata metadata) {
            ensureCompatibility(metadata, "dummy");
        }

        public void ensureCompatibility(final ProjectionMetadata metadata, final String logId) {
            final var selector = ByLogSelector.from(metadata.getSelector(), Events.byLogId(logId));
            if (selector == null) {
                throw new IllegalStateException(String.format(
                        "Projection '%s' cannot be used with %s because it is not appendable!",
                        metadata.getName(),
                        ProjectedLogs.class
                ));
            }
        }

        public <T> ProjectedLog<T> create(final String type, final String name) {
            final var metadata = metadataRegistry.lookup(type);
            ensureCompatibility(metadata, name);
            return new ProjectedLog<>(
                    projectionTargetFactories.lookup(type).create(name),
                    eventStore.stream(ByLogSelector.from(metadata.getSelector(), Events.byLogId(name)))
            );
        }
    }
}
