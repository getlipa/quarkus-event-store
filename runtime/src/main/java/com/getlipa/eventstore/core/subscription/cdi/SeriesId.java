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
public @interface SeriesId {

    String seriesId();

    @Getter
    @Accessors(fluent = true)
    @RequiredArgsConstructor
    class Literal extends AnnotationLiteral<SeriesId> implements SeriesId {

        private final String seriesId;

        public static SeriesId create(final UUID seriesId) {
            return create(seriesId.toString());
        }

        public static SeriesId create(final String seriesId) {
            return new Literal(seriesId);
        }
    }
}
