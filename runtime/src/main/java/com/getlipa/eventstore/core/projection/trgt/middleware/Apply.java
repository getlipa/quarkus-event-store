package com.getlipa.eventstore.core.projection.trgt.middleware;

import jakarta.inject.Qualifier;

import java.lang.annotation.*;

@Qualifier
@Repeatable(Apply.Combined.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Apply {

    Class<?> value();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Combined {

        Apply[] value();

    }
}
