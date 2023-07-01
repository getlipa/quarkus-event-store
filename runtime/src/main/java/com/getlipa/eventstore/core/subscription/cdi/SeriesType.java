package com.getlipa.eventstore.core.subscription.cdi;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.UUID;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
public @interface SeriesType {

    String value();

    @Getter
    @Accessors(fluent = true)
    @RequiredArgsConstructor
    class Literal extends AnnotationLiteral<SeriesType> implements SeriesType {

        private final String value;

        public static SeriesType create(final UUID value) {
            return new Literal(value == null ? "" : value.toString()); // FIXME
        }
    }
}
