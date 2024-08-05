package com.getlipa.eventstore.projection.extension;

import com.getlipa.eventstore.projection.ProjectionMetadata;

/*
TODO: Replace by:
extensionFactories.select(NamedLiteral.of(projectionMetadata))
                    .getHandle()
                    .getBean()
                    .create(CONTEXT) // how to obtain creational context and enrich with metadata without scope?
 */
public interface ExtensionFactory {

    ProjectionExtension create(ProjectionMetadata projectionMetadata);

}
