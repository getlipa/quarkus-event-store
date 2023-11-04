package com.getlipa.eventstore.core.projection.projector.scope;

import com.getlipa.eventstore.core.Registry;
import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.projection.cdi.Projection;
import com.getlipa.eventstore.core.projection.projector.Projector;
import com.getlipa.eventstore.core.projection.projector.ProjectorGateway;
import com.getlipa.eventstore.core.projection.trgt.ProjectionTarget;
import com.getlipa.eventstore.subscriptions.Projections;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.Vertx;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@QuarkusTest
@ExtendWith(MockitoExtension.class)
class ProjectorTest {

    private static final ProjectionTarget.Id ID = ProjectionTarget.Id.create("test", "name");

    @Inject
    Vertx vertx;

    @Inject
    Registry<ProjectorGateway> projectorGatewayRegistry;

    @Mock
    ProjectionTarget<TestProjection> projectionTarget;

    @BeforeEach
    void setup() {
        vertx.deployVerticle(Projector.createFor(
                null,
                projectionTarget,
                ProjectorScope.create(ID)
        ));
    }

    @Test
    void handle() {
        projectorGatewayRegistry.lookup(ID).deliver(Event.builder().withPayload(Projections.Event.getDefaultInstance()));
    }

    @Projection(name = "test")
    public static class TestProjection {

    }
}