package com.getlipa.eventstore.core.projection.projector.commands;

import com.getlipa.eventstore.core.event.AnyEvent;
import com.getlipa.eventstore.core.projection.projector.instance.InstanceManager;
import com.getlipa.eventstore.core.projection.projector.instance.state.Unregistered;
import com.getlipa.eventstore.core.projection.trgt.ProjectionTarget;
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
class ProjectRequestTest {

    ProjectRequest projectRequest;

    @Mock
    ProjectionTarget.Id targetId;

    @Mock
    Vertx vertx;

    @Mock
    InstanceManager instanceManager;

    @Mock
    AnyEvent event;

    @Mock
    Unregistered unregistered;

    @BeforeEach
    void setup() {
        projectRequest = ProjectRequest.create(targetId, vertx, instanceManager, event);

        doReturn(Future.succeededFuture()).when(unregistered).process(projectRequest);
    }

    @Test
    void proceed_failed() {
        projectRequest.proceed(unregistered);
        final var secondResult = projectRequest.proceed(unregistered);

        assertTrue(secondResult.isComplete());
        assertTrue(secondResult.failed());
    }
}