package com.getlipa.eventstore.projection.projector;

import com.getlipa.eventstore.projection.projector.deployment.DeploymentManager;
import com.getlipa.eventstore.projection.Companion;
import com.getlipa.eventstore.aggregate.hydration.AggregateHydratorFactory;
import com.getlipa.eventstore.projection.projector.deployment.ProjectorDeployment;
import com.getlipa.eventstore.projection.projector.scope.ProjectorScopeContext;
import io.quarkus.runtime.StartupEvent;
import io.vertx.core.Promise;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.spi.VerticleFactory;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

@Slf4j
@RequiredArgsConstructor
public class ProjectorFactory implements VerticleFactory {

    public static final String PREFIX = "projector";

    @Inject
    DeploymentManager deploymentManager;

    @Inject
    Companion<AggregateHydratorFactory> factories;

    public Verticle create(final DeploymentManager deploymentManager, final ProjectorId id) {
        return ProjectorScopeContext.get(id)
                .compute(scope -> ProjectorDeployment.createFor(
                        deploymentManager,
                        factories.lookup(id.getProjectionName()).create(id.getId()),
                        scope
                ));
    }

    @Override
    public String prefix() {
        return PREFIX;
    }

    @Override
    public void createVerticle(String verticleName, ClassLoader classLoader, Promise<Callable<Verticle>> promise) {
        promise.complete(() -> {
            final var targetId = VerticleFactory.removePrefix(verticleName);
            log.trace("Initializing Projector verticle: {}", targetId);
            return create(deploymentManager, ProjectorId.fromVerticleId(targetId));
        });
    }

    void onStart(@Observes StartupEvent event, Vertx vertx) {
        vertx.registerVerticleFactory(this);
        log.debug("VerticleFactory registered for prefix: {}", PREFIX);
    }
}
