package com.getlipa.eventstore.core.projection.trgt;

import com.getlipa.eventstore.core.projection.trgt.context.InitializeContext;
import com.getlipa.eventstore.core.projection.trgt.context.ApplyContext;
import com.getlipa.eventstore.core.projection.trgt.context.RefreshContext;
import io.vertx.core.Future;

public abstract class ProjectionStep<T> {

    abstract public Future<T> skip();

    abstract public Future<T> initialize(InitializeContext<T> context);

    abstract public Future<T> apply(ApplyContext<T> context);

    abstract public Future<T> refresh(RefreshContext<T> context);
}
