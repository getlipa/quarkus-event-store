package com.getlipa.eventstore.core.projection.cdi;

import com.getlipa.eventstore.core.projection.projector.DispatchStrategy;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.lang.annotation.*;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
public @interface Events {


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
    @interface WithType {

        String value();

        @Getter
        @Accessors(fluent = true)
        @RequiredArgsConstructor
        class Literal extends AnnotationLiteral<WithType> implements WithType {

            private final String value;

            public static WithType create(final String value) {
                return new Literal(value);
            }
        }
    }

    @Events
    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    @interface WithCorrelation {

        String value();

        @Getter
        @Accessors(fluent = true)
        @RequiredArgsConstructor
        final class Literal extends AnnotationLiteral<WithCorrelation> implements WithCorrelation {

            private final String value;

            public static WithCorrelation create(final String value) {
                return new Literal(value);
            }
        }
    }

    @Getter
    @Accessors(fluent = true)
    @RequiredArgsConstructor
    abstract class AnyLiteral<T extends Annotation> extends AnnotationLiteral<T> {

        private final String value;
    }

    @Events
    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    @interface Any {

        String value();

        final class Literal extends AnyLiteral<Any.Literal> implements WithCorrelation {


            public Literal(String value) {
                super(value);
            }

            public static WithCorrelation create(final String value) {
                return new Literal(value);
            }
        }
    }

    public static

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    @interface WithCorrelationId {

        String value();
    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    @interface WithCausationId {

        String value();
    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    @interface WithLogDomain {

        String value();
    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    @interface WithLogId {

        String value();
    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    @interface Dispatch {

        DispatchStrategy value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    @interface Resolve {
    }
}
