package com.getlipa.eventstore.core.projection.trgt.context;

import com.getlipa.eventstore.core.projection.trgt.ProjectionStep;
import com.getlipa.eventstore.core.projection.trgt.ProjectionTarget;
import io.vertx.core.Future;

public class RefreshContext<T> extends AbstractContext<T> {

    public RefreshContext(final ProjectionTarget<T> target, final ProjectionStep<T> projectionStep) {
        super(target, projectionStep);
    }

    @Override
    public Future<T> proceed() {
        return projectionStep.refresh(this);
    }

    public RefreshContext<T> advance(final ProjectionStep<T> nextStep) {
        return new RefreshContext<>(
                target,
                nextStep
        );
    }
}
