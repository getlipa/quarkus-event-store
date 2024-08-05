package com.getlipa.eventstore.projection.cdi;

import com.getlipa.eventstore.projection.projector.scope.ProjectorScoped;
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

    String context() default "";

}
