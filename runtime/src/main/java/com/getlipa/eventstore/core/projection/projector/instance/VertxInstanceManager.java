package com.getlipa.eventstore.core.projection.projector.instance;

import com.getlipa.eventstore.core.projection.projector.Projector;
import com.getlipa.eventstore.core.projection.trgt.ProjectionTarget;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.Counter;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class VertxInstanceManager implements InstanceManager {

    public static final int NOT_DEPLOYED_COUNTER_VALUE = 0;

    private final Vertx vertx;

    @Override
    public Future<Projector.Id> resolve(final ProjectionTarget.Id targetId) {
        return vertx.sharedData().getCounter(targetId.toString())
                .flatMap(Counter::get)
                .map(instanceId -> Projector.Id.create(targetId, instanceId));
    }

    @Override
    public Future<Void> register(Projector.Id projectorId) {
        return vertx.sharedData().getCounter(projectorId.getTargetId().toString())
                .flatMap(counter -> counter.get().flatMap(value -> counter.compareAndSet(value, projectorId.getInstanceId())))
                .flatMap(updated -> {
                    if (updated) {
                        return Future.succeededFuture();
                    }
                    return Future.failedFuture("Another instance has already been registered.");
                });
    }

    @Override
    public Future<Void> unregister(Projector.Id projectorId) {
        return vertx.sharedData().getCounter(projectorId.getTargetId().toString())
                .flatMap(counter -> counter.compareAndSet(projectorId.getInstanceId(), NOT_DEPLOYED_COUNTER_VALUE))
                .flatMap(unregistered -> {
                    if (!unregistered) {
                        log.warn("Detected stale projector while unregistering: {}", projectorId);
                    }
                    return Future.succeededFuture();
                });
    }
}
