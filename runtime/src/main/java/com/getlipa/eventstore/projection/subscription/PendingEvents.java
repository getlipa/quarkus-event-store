package com.getlipa.eventstore.projection.subscription;

import com.getlipa.eventstore.event.AnyEvent;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PendingEvents {

    private final Set<Long> positions = new HashSet<>(List.of(Long.MAX_VALUE));

    private Promise<Void> cleared;

    public static PendingEvents create() {
        return new PendingEvents();
    }

    public void add(AnyEvent event) {
        if (positions.isEmpty()) {
            cleared = Promise.promise();
        }
        positions.add(event.getPosition());
    }

    public void remove(AnyEvent event) {
        positions.remove(event.getPosition());
        if (positions.isEmpty()) {
            cleared.complete();
        }
    }

    public Future<Void> cleared() {
        if (cleared == null) {
            return Future.succeededFuture();
        }
        return cleared.future();
    }

    public long checkpoint() {
        if (positions.isEmpty()) {
            return Long.MAX_VALUE;
        }
        return Collections.min(positions);
    }
}
