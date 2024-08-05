package com.getlipa.eventstore.projection;

import com.getlipa.eventstore.projection.cdi.ProjectionCompanion;
import com.getlipa.eventstore.projection.cdi.ProjectionName;
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
public class Companion<T> {

    private final Class<T> type;

    private final Instance<T> beans;

    public T lookup(ProjectionMetadata metadata) {
        return lookup(metadata.getName());
    }

    public T lookup(String name) {
        final var instance = beans.select(ProjectionName.Literal.create(name));
        if (instance.isAmbiguous()) {
            throw new IllegalStateException(String.format("Ambiguous companion for projection: %s / %s", type, name));
        }
        if (!instance.isResolvable()) {
            throw new IllegalStateException(String.format("No such companion for projection: %s / %s ", type, name));
        }
        return instance.get();
    }

    static class Producer {

        @Inject
        @ProjectionCompanion
        Instance<Object> beans;

        @Produces
        @Dependent
        @SuppressWarnings("unchecked")
        <T> Companion<T> produce(InjectionPoint injectionPoint) throws ClassNotFoundException {
            final var parameterizedType = (ParameterizedType) injectionPoint.getType();
            final var genericTypeClass = parameterizedType.getActualTypeArguments()[0];
            final Class<T> type = (Class<T>) Class.forName(genericTypeClass.getTypeName());
            return new Companion<>(type, beans.select(type));
        }
    }
}
