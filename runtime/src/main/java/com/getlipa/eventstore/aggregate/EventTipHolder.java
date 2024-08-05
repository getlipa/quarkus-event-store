package com.getlipa.eventstore.aggregate;

import com.getlipa.eventstore.aggregate.middleware.AggregateMiddleware;
import com.getlipa.eventstore.aggregate.middleware.steps.ApplyStep;
import com.getlipa.eventstore.event.AnyEvent;
import io.vertx.core.Future;
import lombok.Getter;

// TODO: Get rid of this class -> integrate into AggregateHydrator!!

@Getter
class EventTipHolder<T> extends AggregateMiddleware<T> {

    private AnyEvent event;

    public static <T> EventTipHolder<T> create() {
        return new EventTipHolder<>();
    }

    @Override
    public Future<T> project(ApplyStep<T> context) {
        return context.proceed()
                .andThen(ok -> event = context.getEvent());
    }
}
