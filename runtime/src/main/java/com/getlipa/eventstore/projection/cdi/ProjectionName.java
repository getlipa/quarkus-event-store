package com.getlipa.eventstore.projection.cdi;

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
public @interface ProjectionName {
    String value();

    @Getter
    @Accessors(fluent = true)
    @RequiredArgsConstructor
    class Literal extends AnnotationLiteral<ProjectionName> implements ProjectionName {

        private final String value;

        public static ProjectionName create(final String name) {
            return new Literal(name);
        }
    }
}
