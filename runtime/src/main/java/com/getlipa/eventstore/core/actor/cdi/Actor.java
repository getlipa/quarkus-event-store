package com.getlipa.eventstore.core.actor.cdi;

import jakarta.enterprise.inject.Stereotype;
import jakarta.enterprise.util.AnnotationLiteral;
import lombok.AccessLevel;
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
@Target(ElementType.TYPE)
@Actor.InterceptorBinding
public @interface Actor {

    String value();

    @jakarta.inject.Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
    @interface Qualifier {
    }

    @jakarta.interceptor.InterceptorBinding
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR})
    @interface InterceptorBinding {
    }

    @jakarta.inject.Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
    @interface Type {
        String value();

        @Getter
        @Accessors(fluent = true)
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        class Literal extends AnnotationLiteral<Type> implements Type {

            private final String value;

            public static Literal create(String id) {
                return new Literal(id);
            }
        }
    }

}
