package com.getlipa.eventstore.aggregate.middleware.steps;

import com.getlipa.eventstore.aggregate.context.Context;
import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.hydration.Hydrator;
import com.getlipa.eventstore.identifier.Id;
import io.vertx.core.Future;

public class InitializeStep<T> extends AbstractStep<T> {

    public InitializeStep(
            final Id targetId,
            final Context context,
            final Hydrator<T> origin,
            final Hydrator<T> hydrator
    ) {
        super(targetId, context,origin, hydrator);
    }

    @Override
    public Future<T> proceed() {
        return hydrator.initialized();
        //return next.initialize(this);
    }

    public Future<T> apply(AnyEvent event) {
        return hydrator.apply(event);
    }

}
