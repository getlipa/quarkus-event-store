package com.getlipa.eventstore.core.projection.projected;

import com.getlipa.eventstore.core.projection.ProjectionMetadata;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProjectedStreams<T> {

    private final ProjectionMetadata metadata;

    private final ProjectedStream.Factory projectedStreamFactory;

    public ProjectedStream<T> get(String id) {
        return projectedStreamFactory.create(metadata.getName(), id);
    }
}
