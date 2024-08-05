package com.getlipa.eventstore.projection.subscription;

import com.getlipa.eventstore.EventStore;
import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.event.selector.Selector;
import io.vertx.core.Future;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashSet;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@ApplicationScoped
public class SubscriptionManager {

    private final Set<AnyEvent.Handler<Void>> handlers = new LinkedHashSet<>();

    public void on(@ObservesAsync EventStore.EventAppended eventAppended) {
        handlers.forEach(handler -> handler.handle(eventAppended.getEvent()));
    }

    public Subscription subscribe(Selector selector, AnyEvent.Handler<?> handler) {
        handlers.add(event -> {
            if (selector.matches(event)) {
                return handler.handle(event).mapEmpty();
            }
            return Future.succeededFuture();
        });
        return () -> handlers.remove(handler);
    }
}
