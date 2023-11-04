package com.getlipa.eventstore.core.projection.projector;

import com.getlipa.eventstore.core.event.AnyEvent;
import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.projection.projector.instance.InstanceManager;
import com.getlipa.eventstore.core.projection.projector.scope.ProjectorScope;
import com.getlipa.eventstore.core.projection.trgt.ProjectionTarget;
import com.getlipa.eventstore.core.proto.ProtoCodec;
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
class ProjectorTest {

    private static final ProjectionTarget.Id ID = ProjectionTarget.Id.create("test", "name");

    @Inject
    Vertx vertx;

    @InjectMock
    InstanceManager instanceManager;

    ProjectionTarget<Object> projectionTarget;

    ProjectorScope projectorScope;

    String deploymentId;

    Projector.Id projectorId;

    @BeforeEach
    void setupInstanceManager() {
        doReturn(Future.succeededFuture()).when(instanceManager).unregister(any());
    }

    @BeforeEach
    void setupProjectionTarget() {
        projectionTarget = mock(ProjectionTarget.class);
        doReturn(Future.succeededFuture()).when(projectionTarget).initialized();
        doReturn(Future.succeededFuture()).when(projectionTarget).apply(any());
    }

    @BeforeEach
    void setupVertx() {
        vertx.eventBus()
                // FIXME
                .unregisterCodec(Projector.CODEC)
                .registerCodec(ProtoCodec.create(
                        Projector.CODEC,
                        AnyEvent::toProto,
                        Event::from
                ));
    }


    @BeforeEach
    void setupProjector() {
        vertx = spy(vertx);
        Projector.vertxOverride = vertx;
        projectorScope = spy(ProjectorScope.create(ID));
        deploymentId = vertx.deployVerticle(Projector.createFor(instanceManager, projectionTarget,projectorScope))
                .onSuccess(deploymentId -> projectorId = Projector.Id.create(ID, deploymentId))
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
        vertx.undeploy(deploymentId).toCompletionStage().toCompletableFuture().join();

        verify(projectorScope).destroy();
        verify(instanceManager).unregister(projectorId);
    }

    @Test
    void handle() {
        final var event = mock(AnyEvent.class);
        final var result = sendEvent(event);

        assertTrue(result.isComplete());
        assertTrue(result.succeeded());

        verify(projectionTarget).apply(event);
    }

    @Test
    void handle_invalid() {
        final var event = mock(Object.class);
        final var result = sendEvent(event);

        assertTrue(result.isComplete());
        assertTrue(result.failed());

        verify(projectionTarget, never()).apply(any());
    }

    @Test
    void undeploy() {
        // TODO: move timer & tick -> allow for configuration first!!
    }

    Future<Message<AnyEvent>> sendEvent(Object event) {
        final var result = vertx.eventBus().<AnyEvent>request(
                projectorId.toString(),
                event,
                new DeliveryOptions().setCodecName(Projector.CODEC)
        );
        try {
            result.toCompletionStage().toCompletableFuture().join();
        } catch (Exception ignored) {
        }
        return result;
    }
}