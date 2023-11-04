package com.getlipa.eventstore.core.projection.trgt.context;

import com.getlipa.eventstore.core.event.AnyEvent;
import com.getlipa.eventstore.core.projection.trgt.ProjectionStep;
import com.getlipa.eventstore.core.projection.trgt.ProjectionTarget;
import io.vertx.core.Future;

public class InitializeContext<T> extends AbstractContext<T> {

    public InitializeContext(final ProjectionTarget<T> target, final ProjectionStep<T> projectionStep) {
        super(target, projectionStep);
    }

    @Override
    public Future<T> proceed() {
        return projectionStep.initialize(this);
    }

    public Future<T> apply(AnyEvent event) {
        return target.apply(event);
    }

    public InitializeContext<T> advance(final ProjectionStep<T> nextStep) {
        return new InitializeContext<>(
                target,
                nextStep
        );
    }
}
