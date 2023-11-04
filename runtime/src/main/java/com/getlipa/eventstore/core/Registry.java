package com.getlipa.eventstore.core;

import com.getlipa.eventstore.core.projection.cdi.Projection;
import com.getlipa.eventstore.core.projection.ProjectionMetadata;
import com.getlipa.eventstore.core.projection.trgt.ProjectionTarget;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;

@Slf4j
@RequiredArgsConstructor
public class Registry<T> {

    private final Class<T> type;

    private final Instance<T> beans;

    public T lookup(InjectionPoint injectionPoint) {
        final var parameterizedType = (ParameterizedType) injectionPoint.getType();
        final var genericTypeClass = parameterizedType.getActualTypeArguments()[0];
        return lookup(genericTypeClass.getTypeName());
    }

    public T lookup(ProjectionTarget.Id id) {
        return lookup(id.getType());
    }

    public T lookup(ProjectionMetadata metadata) {
        return lookup(metadata.getName());
    }

    public T lookup(String name) {
        final var instance = beans.select(Projection.Named.Literal.create(name));
        if (instance.isAmbiguous()) {
            throw new IllegalStateException(String.format("Ambiguous bean for projection: %s / %s", type, name));
        }
        if (!instance.isResolvable()) {
            throw new IllegalStateException(String.format("No such bean for projection: %s / %s ", type, name));
        }
        return instance.get();
    }

    static class Producer {

        @Inject
        @Projection.Any
        Instance<Object> beans;

        @Produces
        @Dependent
        @SuppressWarnings("unchecked")
        <T> Registry<T> produce(InjectionPoint injectionPoint) throws ClassNotFoundException {
            final var parameterizedType = (ParameterizedType) injectionPoint.getType();
            final var genericTypeClass = parameterizedType.getActualTypeArguments()[0];
            final Class<T> type = (Class<T>) Class.forName(genericTypeClass.getTypeName());
            return new Registry<>(type, beans.select(type));
        }
    }
}
