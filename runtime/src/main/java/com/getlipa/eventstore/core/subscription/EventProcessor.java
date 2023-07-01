package com.getlipa.eventstore.core.subscription;

import com.getlipa.eventstore.core.event.AnyEvent;
import io.vertx.core.Future;

public interface EventProcessor {

    Future<Void> process(AnyEvent event);
}
