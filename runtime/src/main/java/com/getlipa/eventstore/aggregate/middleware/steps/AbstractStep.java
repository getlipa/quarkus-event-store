package com.getlipa.eventstore.aggregate.middleware.steps;

import com.getlipa.eventstore.aggregate.context.Context;
import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.hydration.Hydrator;
import com.getlipa.eventstore.identifier.Id;
import io.vertx.core.Future;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractStep<T> {

    @Getter
    protected final Id aggregateId;

    @Getter
    protected final Context context;

    protected final Hydrator<T> origin;

    protected final Hydrator<T> hydrator;

    public Future<T> apply(AnyEvent event) {
        return origin.apply(event);
    }

    public Future<T> abort() {
        return Future.succeededFuture(hydrator.get());
    }

    public T get() {
        return hydrator.get();
    }

    public abstract Future<T> proceed();
}
