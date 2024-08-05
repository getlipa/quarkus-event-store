package com.getlipa.eventstore.projection.projector.commands;

import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.projection.projector.deployment.DeploymentManager;
import com.getlipa.eventstore.projection.projector.deployment.state.Unregistered;
import com.getlipa.eventstore.projection.projector.ProjectorId;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static io.smallrye.common.constraint.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ApplyRequestTest {

    ApplyRequest applyRequest;

    @Mock
    ProjectorId projectorId;

    @Mock
    Vertx vertx;

    @Mock
    DeploymentManager deploymentManager;

    @Mock
    AnyEvent event;

    @Mock
    Unregistered unregistered;

    @BeforeEach
    void setup() {
        applyRequest = ApplyRequest.create(projectorId, vertx, deploymentManager, event);

        doReturn(Future.succeededFuture()).when(unregistered).process(applyRequest);
    }

    @Test
    void proceed_failed() {
        applyRequest.proceed(unregistered);
        final var secondResult = applyRequest.proceed(unregistered);

        assertTrue(secondResult.isComplete());
        assertTrue(secondResult.failed());
    }
}