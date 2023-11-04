package com.getlipa.eventstore.core.projection.trgt.context;

import com.getlipa.eventstore.core.event.AnyEvent;
import com.getlipa.eventstore.core.projection.trgt.ProjectionStep;
import com.getlipa.eventstore.core.projection.trgt.ProjectionTarget;
import io.vertx.core.Future;
import lombok.Getter;

@Getter
public class ApplyContext<T> extends AbstractContext<T> {

    private final AnyEvent event;

    public ApplyContext(
            final ProjectionTarget<T> target,
            ProjectionStep<T> next,
            final AnyEvent event
    ) {
        super(target, next);
        this.event = event;
    }

    @Override
    public Future<T> proceed() {
        return projectionStep.apply(this);
    }

    public ApplyContext<T> withNext(final ProjectionStep<T> next) {
        return new ApplyContext<>(
                target,
                next,
                event
        );
    }
}
