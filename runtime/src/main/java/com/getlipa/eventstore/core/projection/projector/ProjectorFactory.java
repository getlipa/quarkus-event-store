package com.getlipa.eventstore.core.projection.projector;

import com.getlipa.eventstore.core.Registry;
import com.getlipa.eventstore.core.projection.projector.instance.InstanceManager;
import com.getlipa.eventstore.core.projection.trgt.ProjectionTarget;
import com.getlipa.eventstore.core.projection.trgt.ProjectionTargetFactory;
import com.getlipa.eventstore.core.projection.projector.scope.ProjectorScopeContext;
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
    InstanceManager instanceManager;

    @Inject
    Registry<ProjectionTargetFactory> projectionTargetFactoryRegistry;

    public Verticle create(final InstanceManager instanceManager, final ProjectionTarget.Id id) {
        return ProjectorScopeContext.get(id)
                .compute(scope -> Projector.createFor(
                        instanceManager,
                        projectionTargetFactoryRegistry.lookup(id.getType()).create(id.getName()),
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
            return create(instanceManager, ProjectionTarget.Id.create(targetId));
        });
    }

    void onStart(@Observes StartupEvent event, Vertx vertx) {
        vertx.registerVerticleFactory(this);
        log.debug("VerticleFactory registered for prefix: {}", PREFIX);
    }
}
