package com.getlipa.eventstore.projection.projector.scope;

import jakarta.enterprise.context.NormalScope;
import jakarta.inject.Scope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@NormalScope
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD })
@Scope
public @interface ProjectorScoped {
}
