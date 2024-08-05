package com.getlipa.eventstore.aggregate.middleware.steps;

import com.getlipa.eventstore.aggregate.context.Context;
import com.getlipa.eventstore.hydration.Hydrator;
import com.getlipa.eventstore.identifier.Id;
import io.vertx.core.Future;

public class RefreshStep<T> extends AbstractStep<T> {

    public RefreshStep(
            final Id targetId,
            final Context context,
            final Hydrator<T> origin,
            final Hydrator<T> hydrator
    ) {
        super(targetId, context, origin, hydrator);
    }

    @Override
    public Future<T> proceed() {
        return hydrator.refreshed();
    }
}
