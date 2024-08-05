package com.getlipa.eventstore.hydration;

import com.getlipa.eventstore.event.AnyEvent;
import io.vertx.core.Future;

public interface Hydrator<T> {

    T get();

    Class<T> getType();

    Future<T> apply(AnyEvent event);

    Future<T> initialized();
    Future<T> refreshed();

}
