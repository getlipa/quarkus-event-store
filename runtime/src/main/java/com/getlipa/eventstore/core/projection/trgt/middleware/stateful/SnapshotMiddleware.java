package com.getlipa.eventstore.core.projection.trgt.middleware.stateful;

import com.getlipa.eventstore.core.EventStore;
import com.getlipa.eventstore.core.projection.trgt.ProjectionTarget;
import com.getlipa.eventstore.core.projection.trgt.context.InitializeContext;
import io.vertx.core.Future;
import jakarta.enterprise.context.Dependent;
import lombok.RequiredArgsConstructor;

@Dependent
@RequiredArgsConstructor
public class SnapshotMiddleware<T> extends ProjectionTarget.Middleware<Snapshottable<T>> {

    private final EventStore eventStore;

    private long currentPosition = -1;
    private boolean isCaughtUp = false;



    /*
    @Override
    protected void load(OldLoadContext<Snapshottable<T>> loadContext) throws Exception {
        final var snapshottable = loadContext.proceed();
        snapshottable.loadSnapshot(null);
    }

    @Override
    protected Future<Void> project(OldProjectContext<Snapshottable<T>> projectContext) {
        return projectContext.proceed();
    }



    @InterceptorBinding
    @Target( { METHOD, TYPE } )
    @Retention( RUNTIME )
    public static @interface Interceptor {

    }

     */

    // TODO: Allow middlewares to define get() as well -> ensure aggregate is loaded when get is called!
    // --> NO, USE MIDDLEWARE PRIORITY INSTEAD!!!


    @Override
    protected Future<Snapshottable<T>> init(InitializeContext<Snapshottable<T>> context) {
        return context.proceed();/*
        return initContext.proceed()
                .flatMap(v -> initContext.state())
                .onSuccess(snapshottable -> snapshottable.loadSnapshot(null))
                .mapEmpty();
               // .onSuccess(r -> initContext.get().loadSnapshot(null));
               */
    }
}
