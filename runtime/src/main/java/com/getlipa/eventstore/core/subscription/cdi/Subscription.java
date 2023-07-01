package com.getlipa.eventstore.core.subscription.cdi;

import jakarta.enterprise.inject.Stereotype;
import com.getlipa.eventstore.core.actor.cdi.ActorScoped;
import jakarta.enterprise.util.AnnotationLiteral;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Stereotype
@ActorScoped
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
@Stream
public @interface Subscription {

    String value();

    @jakarta.inject.Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
    @interface Qualifier {
    }

    @jakarta.inject.Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
    @interface Type {

        String DEFAULT = "ephemeral";

        String value();

        @Getter
        @Accessors(fluent = true)
        @RequiredArgsConstructor
        class Literal extends AnnotationLiteral<Type> implements Type {

            private final String value;

            public static Type create(final String seriesId) {
                return new Literal(seriesId);
            }
        }
    }

    @jakarta.inject.Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
    @interface Name {
        String value();

        @Getter
        @Accessors(fluent = true)
        @RequiredArgsConstructor
        class Literal extends AnnotationLiteral<Name> implements Name {

            private final String value;

            public static Name create(final String seriesId) {
                return new Literal(seriesId);
            }
        }
    }
}
