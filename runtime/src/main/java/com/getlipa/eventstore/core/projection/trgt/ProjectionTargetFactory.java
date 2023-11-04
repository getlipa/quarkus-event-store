package com.getlipa.eventstore.core.projection.trgt;

import com.getlipa.eventstore.core.projection.cdi.Projection;
import com.getlipa.eventstore.core.projection.ProjectionMetadata;
import com.getlipa.eventstore.core.projection.cdi.ProjectionTarget.Any;
import com.getlipa.eventstore.core.projection.trgt.eventhandler.EventHandlerInvoker;
import io.quarkus.arc.SyntheticCreationalContext;
import io.quarkus.arc.Unremovable;
import io.quarkus.runtime.annotations.Recorder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class ProjectionTargetFactory {

    private final ProjectionMetadata projectionMetadata;

    private final List<Instance.Handle<ProjectionTarget.Middleware<?>>> middlewares;

    private final Instance.Handle<Object> bean;

    private final EventHandlerInvoker eventHandlerInvoker;

    public static ProjectionTargetFactory create(
            final ProjectionMetadata projectionMetadata,
            final List<Instance.Handle<ProjectionTarget.Middleware<?>>> middlewares,
            final Instance.Handle<Object> bean
    ) {
        return new ProjectionTargetFactory(
                projectionMetadata,
                middlewares,
                bean,
                EventHandlerInvoker.create(bean.getBean().getBeanClass())
        );
    }

    public <T> ProjectionTarget<T> create(final String name) {
        final var middlewareInstances = middlewares.stream()
                .map(handle -> ((ProjectionTarget.Middleware<T>) handle.get()))
                .collect(Collectors.toList());
        return new ProjectionTarget<>(
                projectionMetadata,
                ProjectionTarget.Id.create(projectionMetadata.getName(), name),
                middlewareInstances,
                eventHandlerInvoker,
                () -> (T) bean.get()
        );
    }

    @Slf4j
    @Unremovable
    @ApplicationScoped
    @RequiredArgsConstructor
    static class Factory {

        @Inject
        Instance<ProjectionTarget.Middleware<?>> middlewares;

        @Inject
        @Any
        Instance<Object> beans;

        public ProjectionTargetFactory create(
                final ProjectionMetadata metadata,
                final List<? extends Class<?>> middlewareClasses
        ) {
            final var middlewaresHandles = middlewareClasses.stream()
                    .map(middlewareClass -> middlewares.select((Class<ProjectionTarget.Middleware<?>>) middlewareClass))
                    .map(Instance::getHandle)
                    .collect(Collectors.toUnmodifiableList());
            return ProjectionTargetFactory.create(
                    metadata,
                    middlewaresHandles,
                    beans.select(Projection.Named.Literal.create(metadata.getName())).getHandle()
            );
        }
    }

    @Recorder
    public static class BeanRecorder {

        public Function<SyntheticCreationalContext<Object>, Object> record(
                final ProjectionMetadata metadata,
                final String[] middlewares
        ) {
            final var middlewareClasses = Arrays.stream(middlewares)
                    .map(className -> {
                        try {
                            return Class.forName(className);
                        } catch (ClassNotFoundException e) {
                            throw new IllegalStateException(
                                    String.format(
                                            "Middleware class not found: %s",
                                            className
                                    ),
                                    e
                            );
                        }
                    })
                    .collect(Collectors.toList());
            return context -> CDI.current()
                    .select(ProjectionTargetFactory.Factory.class)
                    .get()
                    .create(metadata, middlewareClasses);
        }
    }
}
