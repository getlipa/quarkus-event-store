package com.getlipa.event.store.it;

import com.getlipa.eventstore.core.projection.checkpointing.ProjectionProgress;
import com.getlipa.eventstore.core.projection.projected.ProjectedLog;
import com.getlipa.eventstore.core.projection.projector.DispatchStrategy;
import com.getlipa.eventstore.core.projection.trgt.middleware.Apply;
import com.getlipa.eventstore.core.projection.trgt.middleware.stateful.CatchUpMiddleware;
import com.getlipa.eventstore.core.projection.trgt.middleware.stateful.SnapshotMiddleware;
import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.projection.trgt.eventhandler.Project;
import com.getlipa.eventstore.core.projection.cdi.Projection;
import com.getlipa.eventstore.core.projection.cdi.Events;
import com.getlipa.eventstore.core.projection.trgt.middleware.stateful.Snapshottable;
import com.getlipa.eventstore.example.event.Example;
import jakarta.enterprise.inject.Instance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Projection(name = "demo-projection")
@Events.WithLogDomain("gugus")
//@Events.WithLogId("gugus") // FIXME: UUID Handling!
@Events.Dispatch(DispatchStrategy.BY_LOG_ID)
@RequiredArgsConstructor
@Slf4j
@Apply(CatchUpMiddleware.class)
@Apply(SnapshotMiddleware.class)
public class DemoProjection implements Snapshottable<String> {



    //@Inject
    Instance<ProjectedLog<ProjectionProgress>> projected;

    @Project

    @Events.Resolve // -> resolve links
    public void onSupplyAdded(Event<Example.Simple> event) {
        log.error("PROJECTION: received event: {}", event);
    }

    //@Override
    public void load(String snapshot) {
        log.error("LOADED: {}", snapshot);
    }

    @Override
    public void loadSnapshot(String snapshot) {
        log.error("LOADED: {}", snapshot);
    }
}



