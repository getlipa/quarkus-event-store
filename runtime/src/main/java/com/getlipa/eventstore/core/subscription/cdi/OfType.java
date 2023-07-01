package com.getlipa.eventstore.core.subscription.cdi;

import com.google.protobuf.Message;
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
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
public @interface OfType {

    Class<? extends Message> value();

    @Getter
    @Accessors(fluent = true)
    @RequiredArgsConstructor
    class Literal extends AnnotationLiteral<OfType> implements OfType {

        private final Class<? extends Message> value;

        public static OfType create(Class<? extends Message> value) {
            return new Literal(value);
        }
    }
}
