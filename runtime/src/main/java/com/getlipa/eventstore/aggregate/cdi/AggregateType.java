package com.getlipa.eventstore.aggregate.cdi;

import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.ParameterizedType;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
public @interface AggregateType {
    String value();

    @Getter
    @Accessors(fluent = true)
    @RequiredArgsConstructor
    class Literal extends AnnotationLiteral<AggregateType> implements AggregateType {

        private final String value;

        public static AggregateType create(final String name) {
            return new Literal(name);
        }

        public static AggregateType from(InjectionPoint injectionPoint) {
            final var parameterizedType = (ParameterizedType) injectionPoint.getType();
            final var genericTypeClass = parameterizedType.getActualTypeArguments()[0];
            return create(genericTypeClass.getTypeName());
        }
    }
}
