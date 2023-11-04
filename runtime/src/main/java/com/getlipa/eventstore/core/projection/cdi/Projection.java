package com.getlipa.eventstore.core.projection.cdi;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Stereotype;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.lang.annotation.*;

@Stereotype
@Dependent
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface Projection {

    String name();

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
    @interface Named {
        String value();

        @Getter
        @Accessors(fluent = true)
        @RequiredArgsConstructor
        class Literal extends AnnotationLiteral<Named> implements Named {

            private final String value;

            public static Named create(final String name) {
                return new Literal(name);
            }
        }
    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
    @interface Any {

        @Getter
        @Accessors(fluent = true)
        @RequiredArgsConstructor
        class Literal extends AnnotationLiteral<Any> implements Any {

            public static Any create() {
                return new Literal();
            }
        }

    }
}
