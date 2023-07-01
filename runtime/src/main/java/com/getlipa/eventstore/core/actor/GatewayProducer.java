package com.getlipa.eventstore.core.actor;

import com.getlipa.eventstore.core.actor.cdi.Actor;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;

import java.lang.reflect.ParameterizedType;

public class GatewayProducer {

    @Inject
    @Actor.Qualifier
    Instance<Object> beans;

    @Produces
    @SuppressWarnings("unchecked")
    <T> Gateway<T> produce(InjectionPoint injectionPoint) {
        final ParameterizedType parameterizedType = (ParameterizedType) injectionPoint.getType();
        final Class<T> genericTypeClass = (Class<T>) parameterizedType.getActualTypeArguments()[0];
        final var actorBeans = beans.select(genericTypeClass);
        if (actorBeans.isAmbiguous()) {
            throw new IllegalStateException("Actor bean for gateway is ambiguous: " + genericTypeClass);
        }
        if (!actorBeans.isResolvable()) {
            throw new IllegalStateException("No actor bean for gateway is found: " + genericTypeClass);
        }
        return new Gateway<T>(actorBeans);
    }


}