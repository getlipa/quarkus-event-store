package com.getlipa.eventstore.projection.catchup;

import com.getlipa.eventstore.projection.extension.ExtensionFactory;
import com.getlipa.eventstore.projection.ProjectionMetadata;
import com.getlipa.eventstore.projection.catchup.config.CatchUpConfig;
import com.getlipa.eventstore.projection.catchup.config.CatchUpConfigs;
import com.getlipa.eventstore.projection.extension.ProjectionExtension;
import io.quarkus.arc.Unremovable;
import io.vertx.core.Future;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CatchUpExtension implements ProjectionExtension {

    private final ProjectionMetadata projectionMetadata;

    private final CatchUpJob catchUpJob;

    private final CatchUpConfig.Config config;

    @Override
    public Future<Void> onStartup() {
        if (config.atStartup()) {
            return catchUpJob.start();
        }
        log.trace("Catch-up at startup is disabled for projection: {}", projectionMetadata.getName());
        return Future.succeededFuture();
    }

    @Override
    public Future<Void> onShutdown() {
        return Future.succeededFuture();
    }

    @Unremovable
    @ApplicationScoped
    @RequiredArgsConstructor
    public static class Factory implements ExtensionFactory {

        private final CatchUpJob.Factory catchUpJobFactory;

        private final CatchUpConfigs configs;

        public CatchUpExtension create(ProjectionMetadata metadata) {
            return new CatchUpExtension(
                    metadata,
                    catchUpJobFactory.create(metadata),
                    configs.all().get(metadata.getName()).catchUp()
            );
        }
    }
}
