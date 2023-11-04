package com.getlipa.eventstore.core.projection.trgt.context;

import com.getlipa.eventstore.core.event.AnyEvent;
import com.getlipa.eventstore.core.projection.ProjectionMetadata;
import com.getlipa.eventstore.core.projection.trgt.ProjectionStep;
import com.getlipa.eventstore.core.projection.trgt.ProjectionTarget;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractContext<T> {

    protected final ProjectionTarget<T> target;

    protected final ProjectionStep<T> projectionStep;

    public ProjectionTarget.Id getTargetId() {
        return target.getId();
    }

    public ProjectionMetadata getProjectionMetadata() {
        return target.getProjectionMetadata();
    }

    public Future<T> apply(AnyEvent event) {
        return target.apply(event);
    }

    public Future<T> skip() {
        return projectionStep.skip();
    }

    public T get() {
        return target.get();
    }

    public abstract Future<T> proceed();
}
