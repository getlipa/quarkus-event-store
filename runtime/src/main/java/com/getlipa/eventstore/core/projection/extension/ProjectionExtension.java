package com.getlipa.eventstore.core.projection.extension;

import com.getlipa.eventstore.core.projection.ProjectionMetadata;
import io.quarkus.arc.SyntheticCreationalContext;
import io.quarkus.arc.Unremovable;
import io.quarkus.runtime.annotations.Recorder;
import io.vertx.core.Future;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;

import java.util.function.Function;

public interface ProjectionExtension {

    Future<Void> onStartup();

    Future<Void> onShutdown();


    @Unremovable
    @ApplicationScoped
    class Factory {

        @Inject
        Instance<ExtensionFactory> extensionFactories;

        public ProjectionExtension create(ProjectionMetadata projectionMetadata, Class<? extends ExtensionFactory> beanClass) {
            return extensionFactories.select(beanClass)
                    .get()
                    .create(projectionMetadata);
        }

    }

    @Recorder
    public static class BeanRecorder {

        public Function<SyntheticCreationalContext<Object>, Object> record(
                final ProjectionMetadata metadata,
                final String beanClass
        ) {
            return context -> {
                try {
                    return CDI.current()
                            .select(Factory.class)
                            .get()
                            .create(metadata, (Class<? extends ExtensionFactory>) Class.forName(beanClass));
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(
                            String.format(
                                    "Middleware class not found: %s",
                                    beanClass
                            ),
                            e
                    );
                }
            };
        }
    }
}
