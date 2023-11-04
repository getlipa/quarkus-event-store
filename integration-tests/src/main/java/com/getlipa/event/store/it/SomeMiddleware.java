package com.getlipa.event.store.it;

import com.getlipa.eventstore.core.projection.trgt.ProjectionTarget;
import com.getlipa.eventstore.core.projection.trgt.context.ApplyContext;
import io.vertx.core.Future;
import jakarta.enterprise.context.Dependent;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InterceptorBinding;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Dependent
@Slf4j
@SomeMiddleware.Interceptor
@Interceptor
public class SomeMiddleware<T> extends ProjectionTarget.Middleware<SomeMiddleware.Snapshottable<T>> {

    private int state = 0;

    //    @Inject
//    ProjectionMetadata projectionMetadata;


    @Override
    public Future<SomeMiddleware.Snapshottable<T>> project(ApplyContext<Snapshottable<T>> context) {
        log.warn("BEFORE MW" + state++);
        final var result = context.proceed();
        log.warn("AFTER MW");
        return result;
    }

    public static interface Snapshottable<T> {

        public void load(T snapshot);

    }

    @InterceptorBinding
    @Target( { METHOD, TYPE } )
    @Retention( RUNTIME )
    public static @interface Interceptor {

    }
}
