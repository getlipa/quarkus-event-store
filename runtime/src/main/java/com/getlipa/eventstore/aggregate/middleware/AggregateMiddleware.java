package com.getlipa.eventstore.aggregate.middleware;

import com.getlipa.eventstore.aggregate.middleware.steps.ApplyStep;
import com.getlipa.eventstore.aggregate.middleware.steps.InitializeStep;
import com.getlipa.eventstore.aggregate.middleware.steps.RefreshStep;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AggregateMiddleware<T> {

    public Future<T> init(InitializeStep<T> context) {
        return context.proceed();
    }

    public Future<T> project(ApplyStep<T> context) {
        return context.proceed();
    }

    public Future<T> refresh(RefreshStep<T> context) {
        return context.proceed();
    }
}
