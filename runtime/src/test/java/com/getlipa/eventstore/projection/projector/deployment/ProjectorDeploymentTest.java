package com.getlipa.eventstore.projection.projector.deployment;

import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.projection.projector.EventCodec;
import com.getlipa.eventstore.projection.projector.ProjectorId;
import com.getlipa.eventstore.projection.projector.scope.ProjectorScope;
import com.getlipa.eventstore.hydration.Hydrator;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@QuarkusTest
class ProjectorDeploymentTest {

    private static final ProjectorId ID = ProjectorId.createDefault("test");

    @Inject
    Vertx vertx;

    @InjectMock
    DeploymentManager deploymentManager;

    Hydrator<Object> hydrator;

    ProjectorScope projectorScope;

    String verticleId;

    DeploymentId deploymentId;

    @BeforeEach
    void setupInstanceManager() {
        doReturn(Future.succeededFuture()).when(deploymentManager).unregister(any());
    }

    @BeforeEach
    void setupProjectionTarget() {
        hydrator = mock(Hydrator.class);
        doReturn(Future.succeededFuture()).when(hydrator).initialized();
        doReturn(Future.succeededFuture()).when(hydrator).apply(any());
    }

    @BeforeEach
    void setupVertx() {
        vertx.eventBus()
                // FIXME
                .unregisterCodec(EventCodec.NAME)
                .registerCodec(EventCodec.create());
    }


    @BeforeEach
    void setupProjector() {
        vertx = spy(vertx);
        ProjectorDeployment.vertxOverride = vertx;
        projectorScope = spy(ProjectorScope.create(ID));
        verticleId = vertx.deployVerticle(ProjectorDeployment.createFor(deploymentManager, hydrator,projectorScope))
                .onSuccess(verticleId -> deploymentId = DeploymentId.create(ID, verticleId))
                .toCompletionStage()
                .toCompletableFuture()
                .join();
    }

    @Test
    void start() {
        verify(vertx).setTimer(eq(60_000L), any());
    }

    @Test
    void stop() {
        vertx.undeploy(verticleId).toCompletionStage().toCompletableFuture().join();

        verify(projectorScope).destroy();
        verify(deploymentManager).unregister(deploymentId);
    }

    @Test
    void handle() {
        final var event = mock(AnyEvent.class);
        final var result = sendEvent(event);

        assertTrue(result.isComplete());
        assertTrue(result.succeeded());

        verify(hydrator).apply(event);
    }

    @Test
    void undeploy() {
        // TODO: move timer & tick -> allow for configuration first!!
    }

    Future<Message<AnyEvent>> sendEvent(Object event) {
        final var result = vertx.eventBus().<AnyEvent>request(
                deploymentId.toString(),
                event,
                new DeliveryOptions().setCodecName(EventCodec.NAME)
        );
        try {
            result.toCompletionStage().toCompletableFuture().join();
        } catch (Exception ignored) {
        }
        return result;
    }
}