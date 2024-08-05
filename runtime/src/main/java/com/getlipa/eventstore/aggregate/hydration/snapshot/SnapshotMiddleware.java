package com.getlipa.eventstore.aggregate.hydration.snapshot;

import com.getlipa.eventstore.EventStore;
import com.getlipa.eventstore.aggregate.middleware.AggregateMiddleware;
import com.getlipa.eventstore.aggregate.middleware.steps.InitializeStep;
import io.vertx.core.Future;
import jakarta.enterprise.context.Dependent;
import lombok.RequiredArgsConstructor;

@Dependent
@RequiredArgsConstructor
public class SnapshotMiddleware<T> extends AggregateMiddleware<Snapshottable<T>> {

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
    public Future<Snapshottable<T>> init(InitializeStep<Snapshottable<T>> context) {
        return context.proceed();/*
        return initContext.proceed()
                .flatMap(v -> initContext.state())
                .onSuccess(snapshottable -> snapshottable.loadSnapshot(null))
                .mapEmpty();
               // .onSuccess(r -> initContext.get().loadSnapshot(null));
               */
    }
}
