package com.getlipa.eventstore.aggregate.hydration;

import com.getlipa.eventstore.EventStore;
import com.getlipa.eventstore.aggregate.middleware.AggregateMiddleware;
import com.getlipa.eventstore.aggregate.middleware.steps.AbstractStep;
import com.getlipa.eventstore.aggregate.middleware.steps.ApplyStep;
import com.getlipa.eventstore.aggregate.middleware.steps.InitializeStep;
import com.getlipa.eventstore.aggregate.middleware.steps.RefreshStep;
import com.getlipa.eventstore.event.selector.Selector;
import com.getlipa.eventstore.stream.reader.cursor.Cursor;
import io.vertx.core.Future;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Dependent
public class ReHydrateMiddleware extends AggregateMiddleware<Object> {

    @Inject
    EventStore eventStore;

    private boolean isCatchingUp = false;

    private Future<Object> initialized;

    private long currentPosition = -1;

    @Override
    public Future<Object> init(InitializeStep<Object> step) {
        if (initialized != null) {
            return initialized;
        }
        log.trace("Starting initial re-hydration: {}-{}", step.getContext(), step.getAggregateId());
        return initialized = catchUp(step)
                .map(step.get());
    }

    @Override
    public Future<Object> refresh(RefreshStep<Object> step) {
        log.trace(
                "Starting re-hydration from position {}: {}-{}",
                currentPosition,
                step.getContext(),
                step.getAggregateId()
        );
        return catchUp(step)
                .map(step.get());
    }

    Future<Long> catchUp(AbstractStep<Object> step) {
        isCatchingUp = true;
        final var context = step.getContext();
        final Selector selector = context.createSelector(step.getAggregateId());
        log.trace("Starting re-hydration from selector {}", selector);
        return step.proceed()
                .flatMap(vd -> eventStore.stream(selector)
                        .readForward() // TODO: Support backward?
                        .from(Cursor.position(currentPosition + 1))
                        .forEach(event -> step.apply(event).map(state -> currentPosition = event.getPosition()))
                )
                .onSuccess(position -> log.info(
                        "Re-hydration completed: {}-{} @{}",
                        context,
                        step.getAggregateId(),
                        position)
                )
                .onFailure(error -> log.error(
                        "Unable to re-hydrate: {}-{} - {} / {}",
                        context,
                        step.getAggregateId(),
                        error.getClass().getSimpleName(),
                        error.getMessage())
                )
                .onComplete(result -> isCatchingUp = false);
    }


    @Override
    public Future<Object> project(ApplyStep<Object> step) {
        final var event = step.getEvent();
        if (currentPosition >= event.getPosition()) {
            log.trace(
                    "Ignoring event because its position is before the current re-hydration tip: {} - {}",
                    currentPosition,
                    event
            );
            return step.abort();
        }
        return step.proceed()
                .onSuccess(result -> {
                    currentPosition = event.getPosition();
                    if (isCatchingUp) {
                        log.debug("Re-applied event: {}", event); // FIXME: position?
                    }
                });
    }
}
