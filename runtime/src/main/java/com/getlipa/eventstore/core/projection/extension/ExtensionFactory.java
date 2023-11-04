package com.getlipa.eventstore.core.projection.extension;

import com.getlipa.eventstore.core.projection.ProjectionMetadata;
import io.quarkus.arc.SyntheticCreationalContext;
import io.quarkus.runtime.annotations.Recorder;
import jakarta.enterprise.inject.spi.CDI;

import java.util.function.Function;

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
