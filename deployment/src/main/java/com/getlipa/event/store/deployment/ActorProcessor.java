package com.getlipa.event.store.deployment;

import com.getlipa.eventstore.projection.projector.scope.ProjectorScopeContext;
import com.getlipa.eventstore.projection.projector.scope.ProjectorScoped;
import io.quarkus.arc.deployment.*;
import io.quarkus.deployment.annotations.BuildStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ActorProcessor {

    private static final Logger log = LoggerFactory.getLogger(ActorProcessor.class);

    @BuildStep
    public ContextRegistrationPhaseBuildItem.ContextConfiguratorBuildItem projectorContext(ContextRegistrationPhaseBuildItem contextRegistrationPhase) {
        return new ContextRegistrationPhaseBuildItem.ContextConfiguratorBuildItem(contextRegistrationPhase.getContext()
                .configure(ProjectorScoped.class).normal().contextClass(ProjectorScopeContext.class));
    }

    @BuildStep
    public CustomScopeBuildItem registerScope() {
        return new CustomScopeBuildItem(ProjectorScoped.class);
    }

}
