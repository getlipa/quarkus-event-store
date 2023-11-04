package com.getlipa.eventstore.core.projection.checkpointing;

import com.getlipa.eventstore.core.projection.ProjectionMetadata;
import com.getlipa.eventstore.core.projection.extension.ExtensionFactory;
import com.getlipa.eventstore.core.projection.extension.ProjectionExtension;
import io.quarkus.arc.Unremovable;
import io.quarkus.scheduler.Scheduler;
import io.vertx.core.Future;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CatchUpExtension implements ProjectionExtension {

    private final String jobIdentity;

    private final ProjectionMetadata metadata;

    private final Scheduler scheduler;

    private final CatchUpProcess catchUpProcess;

    @Override
    public Future<Void> onStartup() {
        if (!metadata.getName().startsWith("$")) { // TODO: Use config!!!!
           /* scheduler.newJob(jobIdentity)
                    // .setCron(projectionMetadata.get(null))
                  //  .setCron("*///7 * * * * ?") // FIXME
                  /*  .setConcurrentExecution(Scheduled.ConcurrentExecution.SKIP)
                    //.setAsyncTask(...)
                    .setTask(execution -> catchUpProcess.start())
                    .schedule();

                   */
            return catchUpProcess.start();
        }
        return Future.succeededFuture();
    }

    @Override
    public Future<Void> onShutdown() {
        scheduler.unscheduleJob(jobIdentity);
        return Future.succeededFuture();
    }

    @Unremovable
    @ApplicationScoped
    @RequiredArgsConstructor
    public static class Factory implements ExtensionFactory {

        private final Scheduler scheduler;

        private final CatchUpProcess.Factory catchUpProcessFactory;

        public CatchUpExtension create(ProjectionMetadata metadata) {
            return new CatchUpExtension(
                    String.format("catchup-%s", metadata.getName()),
                    metadata,
                    scheduler,
                    catchUpProcessFactory.create(metadata)
            );
        }
    }
}
