package com.getlipa.eventstore.projection.projector.deployment;

import com.getlipa.eventstore.projection.projector.ProjectorId;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.Counter;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class VertxDeploymentManager implements DeploymentManager {

    public static final int NOT_DEPLOYED_COUNTER_VALUE = 0;

    private final Vertx vertx;

    @Override
    public Future<DeploymentId> resolve(final ProjectorId projectorId) {
        return vertx.sharedData().getCounter(projectorId.toString())
                .flatMap(Counter::get)
                .map(instanceId -> DeploymentId.create(projectorId, instanceId));
    }

    @Override
    public Future<Void> register(DeploymentId deploymentId) {
        return vertx.sharedData().getCounter(deploymentId.getProjectorId().toString())
                .flatMap(counter -> counter.get().flatMap(value -> counter.compareAndSet(value, deploymentId.getInstanceId())))
                .flatMap(updated -> {
                    if (updated) {
                        return Future.succeededFuture();
                    }
                    return Future.failedFuture("Another instance has already been registered.");
                });
    }

    @Override
    public Future<Void> unregister(DeploymentId deploymentId) {
        return vertx.sharedData().getCounter(deploymentId.getProjectorId().toString())
                .flatMap(counter -> counter.compareAndSet(deploymentId.getInstanceId(), NOT_DEPLOYED_COUNTER_VALUE))
                .flatMap(unregistered -> {
                    if (!unregistered) {
                        log.warn("Detected stale projector while unregistering: {}", deploymentId);
                    }
                    return Future.succeededFuture();
                });
    }
}
