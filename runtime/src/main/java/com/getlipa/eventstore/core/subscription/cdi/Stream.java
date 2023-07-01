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
public @interface Stream {

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    @interface All {

        final class Literal extends AnnotationLiteral<All> implements All {
            public static All create() {
                return new Literal();
            }
        }
    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    @interface ByType {

        String value();

        @Getter
        @Accessors(fluent = true)
        @RequiredArgsConstructor
        class Literal extends AnnotationLiteral<ByType> implements ByType {

            private final String value;

            public static ByType create(final String value) {
                return new Literal(value);
            }
        }
    }

    @Stream
    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    @interface ByCorrelation {

        String value();

        @Getter
        @Accessors(fluent = true)
        @RequiredArgsConstructor
        final class Literal extends AnnotationLiteral<ByCorrelation> implements ByCorrelation {

            private final String value;

            public static ByCorrelation create(final String value) {
                return new Literal(value);
            }
        }
    }

    @Stream
    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    @interface Any {

        String value();

        @Getter
        @Accessors(fluent = true)
        @RequiredArgsConstructor
        final class Literal extends AnnotationLiteral<ByCorrelation> implements ByCorrelation {

            private final String value;

            public static ByCorrelation create(final String value) {
                return new Literal(value);
            }
        }
    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    @interface ByCorrelationId {

        String value();
    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    @interface BySeriesType {

        String value();
    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    @interface BySeriesId {

        String value();
    }
}
