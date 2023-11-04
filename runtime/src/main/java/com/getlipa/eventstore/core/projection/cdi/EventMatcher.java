package com.getlipa.eventstore.core.projection.cdi;

import com.getlipa.eventstore.core.event.selector.SelectorFactory;
import jakarta.inject.Qualifier;

import java.lang.annotation.*;


@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Repeatable(EventMatcher.Combined.class)
public @interface EventMatcher {

    String FACTORY = "factory";
    String PARAMETER = "parameter";

    SelectorFactory factory();

    String parameter();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Qualifier
    @interface Combined {

        EventMatcher[] value();

    }

}
