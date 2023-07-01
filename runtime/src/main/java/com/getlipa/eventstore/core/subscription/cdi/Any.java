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
@Subscription.Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
public @interface Any {


    @Getter
    @Accessors(fluent = true)
    @RequiredArgsConstructor
    class Literal extends AnnotationLiteral<Any> implements Any {

        public static Any create() {
            return new Literal();
        }
    }
}
