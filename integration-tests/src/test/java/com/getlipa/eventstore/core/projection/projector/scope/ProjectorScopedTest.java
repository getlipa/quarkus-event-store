package com.getlipa.eventstore.core.projection.projector.scope;

import com.getlipa.eventstore.core.projection.trgt.ProjectionTarget;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


// NOTE: It appears this test only works when it is in a different module than the runtime module.
@QuarkusTest
class ProjectorScopedTest {

    private static final ProjectionTarget.Id PROJECTOR_ID = ProjectionTarget.Id.create("projector-type", "projector-name");

    @Inject
    Instance<Bean> bean;

    @Test
    public void shouldCreateOnePerScope() {
        final var firstScope = ProjectorScopeContext.get(PROJECTOR_ID);
        final var firstInstance = firstScope.compute(() -> {
            final var projector = bean.get();
            assertEquals(PROJECTOR_ID.getType(), projector.getProjectorType());
            assertEquals(PROJECTOR_ID.getName(), projector.getProjectorName());
            return projector.self();
        });

        final var secondTargetId = ProjectionTarget.Id.createDefault(PROJECTOR_ID.getType() + "-second");
        final var secondScope = ProjectorScopeContext.get(secondTargetId);
        final var secondInstance = secondScope.compute(() -> {
            final var projector = bean.get();
            assertEquals(secondTargetId.getType(), projector.getProjectorType());
            assertEquals(secondTargetId.getName(), projector.getProjectorName());
            return projector;
        });

        assertSame(firstInstance, firstScope.compute(() -> bean.get().self()));
        assertSame(secondInstance, secondScope.compute(() -> bean.get()));
        assertNotSame(firstInstance, secondInstance);
    }

    @Test
    public void shouldReCreateAfterDestroyed() {
        final var firstScope = ProjectorScopeContext.get(PROJECTOR_ID);
        final var firstInstance = firstScope.compute(() -> bean.get().self());
        firstScope.destroy();

        final var secondScope = ProjectorScopeContext.get(PROJECTOR_ID);
        final var secondInstance = secondScope.compute(() -> bean.get().self());

        assertEquals(firstInstance.getProjectorName(), secondInstance.getProjectorName());
        assertEquals(firstInstance.getProjectorType(), secondInstance.getProjectorType());
        assertNotSame(firstInstance, secondInstance);
    }

    @Test
    public void shouldFailWhenNotScoped() {
        assertThrows(ContextNotActiveException.class, () -> bean.get().self());
    }

    @Getter
    @ProjectorScoped
    public static class Bean {

        private final String projectorType;

        private final String projectorName;

        public Bean(ProjectionTarget.Id id) {
            projectorType = id.getType();
            projectorName = id.getName();
        }

        public Bean self() {
            return this;
        }
    }
}