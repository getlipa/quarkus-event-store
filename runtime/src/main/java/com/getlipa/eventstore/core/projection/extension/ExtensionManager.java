package com.getlipa.eventstore.core.projection.extension;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Future;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class ExtensionManager {

    @Any
    @Inject
    Instance<ProjectionExtension> projectionExtensions;

    public void on(@Observes StartupEvent event) {
        projectionExtensions.forEach(ProjectionExtension::onStartup);
    }

    public void on(@Observes @Priority(1) ShutdownEvent event) {
        final var futures = projectionExtensions.stream().map(ProjectionExtension::onShutdown).collect(Collectors.toList());
        Uni.createFrom().completionStage(Future.all(futures).toCompletionStage())
                .await()
                .indefinitely();
    }
}
