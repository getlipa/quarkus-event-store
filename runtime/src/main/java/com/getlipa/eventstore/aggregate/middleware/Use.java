package com.getlipa.eventstore.aggregate.middleware;

import jakarta.inject.Qualifier;

import java.lang.annotation.*;

@Qualifier
@Repeatable(Use.Combined.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Use {

    Class<?> value();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Combined {

        Use[] value();

    }
}
