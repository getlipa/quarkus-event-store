package com.getlipa.eventstore.projection.projector.scope;

import com.getlipa.eventstore.identifier.Id;
import com.getlipa.eventstore.projection.projector.ProjectorId;
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

    private static final ProjectorId PROJECTOR_ID = ProjectorId.createDefault("projection-name");

    @Inject
    Instance<Bean> bean;

    @Test
    public void shouldCreateOnePerScope() {
        final var firstScope = ProjectorScopeContext.get(PROJECTOR_ID);
        final var firstInstance = firstScope.compute(() -> {
            final var projector = bean.get();
            assertEquals(PROJECTOR_ID.getId(), projector.getId());
            assertEquals(PROJECTOR_ID.getProjectionName(), projector.getProjectionName());
            return projector.self();
        });

        final var secondPorjectorId = ProjectorId.createDefault(PROJECTOR_ID.getProjectionName() + "-second");
        final var secondScope = ProjectorScopeContext.get(secondPorjectorId);
        final var secondInstance = secondScope.compute(() -> {
            final var projector = bean.get();
            assertEquals(secondPorjectorId.getId(), projector.getId());
            assertEquals(secondPorjectorId.getProjectionName(), projector.getProjectionName());
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

        assertEquals(firstInstance.getProjectionName(), secondInstance.getProjectionName());
        assertEquals(firstInstance.getId(), secondInstance.getId());
        assertNotSame(firstInstance, secondInstance);
    }

    @Test
    public void shouldFailWhenNotScoped() {
        assertThrows(ContextNotActiveException.class, () -> bean.get().self());
    }

    @Getter
    @ProjectorScoped
    public static class Bean {

        private final Id id;

        private final String projectionName;

        public Bean(ProjectorId id) {
            this.id = id.getId();
            projectionName = id.getProjectionName();
        }

        public Bean self() {
            return this;
        }
    }
}