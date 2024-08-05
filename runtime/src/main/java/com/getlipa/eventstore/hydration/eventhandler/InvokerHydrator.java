package com.getlipa.eventstore.hydration.eventhandler;

import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.hydration.Hydrator;
import io.vertx.core.Future;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class InvokerHydrator<T> implements Hydrator<T> {

    private final EventHandlerInvoker invoker;

    private final T target;

    @Getter
    private final Class<T> type;

    @Override
    public T get() {
        return target;
    }

    @Override
    public Future<T> apply(AnyEvent event) {
        return invoker.invoke(target, event)
                .map(target);
    }

    @Override
    public Future<T> initialized() {
        return Future.succeededFuture(target); // TODO: Call init method if target implements interface
    }

    @Override
    public Future<T> refreshed() {
        return Future.succeededFuture(target);
    }
}
