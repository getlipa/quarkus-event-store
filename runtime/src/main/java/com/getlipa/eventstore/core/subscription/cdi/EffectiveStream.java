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


@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
public @interface EffectiveStream {

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    @interface ByType {

        String uuid();

        @Getter
        @Accessors(fluent = true)
        @RequiredArgsConstructor
        class Literal extends AnnotationLiteral<ByType> implements ByType {

            private final String uuid;

            public static ByType create(final String uuid) {
                return new Literal(uuid);
            }
        }
    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    @interface ByCorrelationId {

        String uuid();

        @Getter
        @Accessors(fluent = true)
        @RequiredArgsConstructor
        final class Literal extends AnnotationLiteral<ByCorrelationId> implements ByCorrelationId {

            private final String uuid;

            public static ByCorrelationId create(final String uuid) {
                return new Literal(uuid);
            }
        }
    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    @interface BySeriesType {

        String uuid();

        @Getter
        @Accessors(fluent = true)
        @RequiredArgsConstructor
        class Literal extends AnnotationLiteral<BySeriesType> implements BySeriesType {

            private final String uuid;

            public static BySeriesType create(final String uuid) {
                return new Literal(uuid);
            }
        }
    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    @interface BySeriesId {

        String uuid();

        @Getter
        @Accessors(fluent = true)
        @RequiredArgsConstructor
        class Literal extends AnnotationLiteral<BySeriesId> implements BySeriesId {

            private final String uuid;

            public static BySeriesId create(final String uuid) {
                return new Literal(uuid);
            }
        }
    }
}
