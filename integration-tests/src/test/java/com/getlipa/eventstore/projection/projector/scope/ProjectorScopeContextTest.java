package com.getlipa.eventstore.core.projection.projector.scope;

import com.getlipa.eventstore.projection.projector.ProjectorId;
import com.getlipa.eventstore.projection.projector.scope.ProjectorScopeContext;
import com.getlipa.eventstore.projection.projector.scope.ProjectorScoped;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


// NOTE: It appears this test only works when it is in a different module than the runtime module.
@QuarkusTest
class ProjectorScopeContextTest {

    private static final ProjectorId PROJECTOR_ID = ProjectorId.createDefault("projector-id");

    @Inject
    private BeanManager beanManager;

    @Test
    public void shouldActivate() {
        final var scope = ProjectorScopeContext.get(PROJECTOR_ID);
        Assertions.assertThrows(ContextNotActiveException.class, () -> beanManager.getContext(ProjectorScoped.class));
        Assertions.assertTrue(scope.compute(() -> beanManager.getContext(ProjectorScoped.class).isActive()));
        Assertions.assertThrows(ContextNotActiveException.class, () -> beanManager.getContext(ProjectorScoped.class));
    }
}