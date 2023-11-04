package com.getlipa.eventstore.core.projection.projected;

import com.getlipa.eventstore.core.projection.ProjectionMetadata;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProjectedLogs<T> {

    private final ProjectionMetadata metadata;

    private final ProjectedLog.Factory projectedLogFactory;

    public ProjectedLog<T> get(String id) {
        return projectedLogFactory.create(metadata.getName(), id);
    }
}
