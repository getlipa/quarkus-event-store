package com.getlipa.eventstore.aggregate.hydration;

import com.getlipa.eventstore.aggregate.middleware.AggregateMiddleware;
import com.getlipa.eventstore.aggregate.context.Context;
import com.getlipa.eventstore.aggregate.middleware.steps.ApplyStep;
import com.getlipa.eventstore.aggregate.middleware.steps.InitializeStep;
import com.getlipa.eventstore.aggregate.middleware.steps.RefreshStep;
import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.hydration.Hydrator;
import com.getlipa.eventstore.identifier.Id;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor
class AggregateHydrator<T> implements Hydrator<T> {

    private final Id id;

    private final Context context;

    @Delegate
    private final Hydrator<T> hydrator;

    private final AggregateMiddleware<T> aggregateMiddleware;

    public static <T> Hydrator<T> chained(
            final Id id,
            final Context context,
            final Hydrator<T> target,
            final Iterable<AggregateMiddleware<T>> middlewares
    ) {
        Hydrator<T> next = target;
        for (final var middleware : middlewares) {
            next = new AggregateHydrator<>(id, context, next, middleware);
        }
        return next;
    }

    @Override
    public Future<T> apply(AnyEvent event) {
        return aggregateMiddleware.project(new ApplyStep<>(
                id,
                context,
                hydrator,
                hydrator,
                event
        ));
    }

    @Override
    public Future<T> initialized() {
        return aggregateMiddleware.init(new InitializeStep<>(
                id,
                context,
                hydrator,
                hydrator
        ));
    }

    @Override
    public Future<T> refreshed() {
        return aggregateMiddleware.refresh(new RefreshStep<>(
                id,
                context,
                hydrator,
                hydrator
        ));
    }
}
