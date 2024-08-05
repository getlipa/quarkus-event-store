package com.getlipa.eventstore.aggregate.middleware.steps;

import com.getlipa.eventstore.aggregate.context.Context;
import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.hydration.Hydrator;
import com.getlipa.eventstore.identifier.Id;
import io.vertx.core.Future;
import lombok.Getter;

@Getter
public class ApplyStep<T> extends AbstractStep<T> {

    private final AnyEvent event;

    public ApplyStep(
            final Id targetId,
            final Context context,
            final Hydrator<T> origin,
            final Hydrator<T> hydrator,
            final AnyEvent event
    ) {
        super(targetId, context, origin, hydrator);
        this.event = event;
    }

    @Override
    public Future<T> proceed() {
        return hydrator.apply(event);
    }
}
