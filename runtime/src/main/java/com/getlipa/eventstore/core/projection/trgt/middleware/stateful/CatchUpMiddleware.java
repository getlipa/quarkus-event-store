package com.getlipa.eventstore.core.projection.trgt.middleware.stateful;

import com.getlipa.eventstore.core.EventStore;
import com.getlipa.eventstore.core.event.selector.Selector;
import com.getlipa.eventstore.core.projection.trgt.ProjectionTarget;
import com.getlipa.eventstore.core.projection.trgt.context.AbstractContext;
import com.getlipa.eventstore.core.projection.trgt.context.InitializeContext;
import com.getlipa.eventstore.core.projection.trgt.context.ApplyContext;
import com.getlipa.eventstore.core.projection.trgt.context.RefreshContext;
import com.getlipa.eventstore.core.stream.reader.cursor.Cursor;
import io.vertx.core.Future;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Dependent
public class CatchUpMiddleware extends ProjectionTarget.Middleware<Object> {

    @Inject
    EventStore eventStore;

    private boolean initialized = false;

    private long catchUpTip = -1;

    @Override
    protected Future<Object> init(InitializeContext<Object> context) {
        log.trace("Starting initial catch-up: {}", context.getTargetId());
        return catchUp(context)
                .onComplete(result -> initialized = true);
    }

    @Override
    public Future<Object> refresh(RefreshContext<Object> context) {
        log.trace("Starting catch-up from position {}: {}", catchUpTip, context.getTargetId());
        return catchUp(context);
    }

    Future<Object> catchUp(AbstractContext<Object> context) {
        final Selector selector = context.getProjectionMetadata().getSelector();
        return context.proceed()
                .flatMap(vd -> eventStore.stream(selector)
                        .readForward()
                        .from(Cursor.position(catchUpTip))
                        .forEach(event -> {
                            catchUpTip = event.getPosition();
                            log.trace("Re-applying event: {}", event);
                            return context.apply(event);
                        })
                        .map(event -> context.get())
                )
                .onSuccess(position -> log.info(
                        "Catch-up completed: {} @{}",
                        context.getTargetId(),
                        position
                ))
                .onFailure(error -> log.error(
                        "Unable to catchup: {} - {} / {}",
                        context.getTargetId(),
                        error.getClass().getSimpleName(),
                        error.getMessage())
                );
    }

    @Override
    public Future<Object> project(ApplyContext<Object> context) {
        final var event = context.getEvent();
        if (initialized && catchUpTip >= event.getPosition()) {
            log.trace(
                    "Ignoring event because its position is before the current catch-up tip: {} - {}",
                    catchUpTip,
                    event
            );
            return context.skip();
        }
        return context.proceed();
    }
}
