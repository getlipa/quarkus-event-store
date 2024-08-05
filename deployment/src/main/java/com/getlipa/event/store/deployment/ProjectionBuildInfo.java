package com.getlipa.event.store.deployment;

import com.getlipa.eventstore.projection.ProjectionMetadata;

public class ProjectionBuildInfo {

    private final ProjectionMetadata metadata;

    private final String typeName;

    public ProjectionBuildInfo(ProjectionMetadata metadata, String typeName) {
        this.metadata = metadata;
        this.typeName = typeName;
    }

    public ProjectionMetadata getMetadata() {
        return metadata;
    }

    public String getTypeName() {
        return typeName;
    }
}
