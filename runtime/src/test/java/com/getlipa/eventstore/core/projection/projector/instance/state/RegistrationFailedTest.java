package com.getlipa.eventstore.core.projection.projector.instance.state;

import com.getlipa.eventstore.core.projection.projector.Projector;
import com.getlipa.eventstore.core.projection.projector.commands.ProjectRequest;
import com.getlipa.eventstore.core.projection.projector.instance.InstanceManager;
import com.getlipa.eventstore.core.projection.trgt.ProjectionTarget;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RegistrationFailedTest {

    private static final ProjectionTarget.Id TARGET_ID = ProjectionTarget.Id.create("projector-type", "projector-name");

    private static final String DEPLOYMENT_ID = "deployment-id";

    private static final Projector.Id PROJECTOR_ID = Projector.Id.create(
            TARGET_ID,
            DEPLOYMENT_ID
    );

    private ProjectorState projectorState;

    @Mock
    InstanceManager instanceManager;

    @Mock
    Vertx vertx;

    @Mock
    Throwable cause;

    @Mock
    ProjectRequest projectRequest;

    @BeforeEach
    void setup() {
        doReturn(TARGET_ID).when(projectRequest).getTargetId();
        doReturn(vertx).when(projectRequest).getVertx();
        doReturn(instanceManager).when(projectRequest).getInstanceManager();
        doReturn(Future.succeededFuture()).when(projectRequest).proceed(any());
        doReturn(Future.succeededFuture()).when(vertx).undeploy(anyString());
        doReturn(Future.succeededFuture(Projector.Id.create(TARGET_ID, "another-deployment-id")))
                .when(instanceManager)
                .resolve(TARGET_ID);

        projectorState = ProjectorState.registrationFailed(PROJECTOR_ID, DEPLOYMENT_ID, cause);
    }

    @Test
    void process() {
        final var result = projectorState.process(projectRequest);

        assertTrue(result.isComplete());
        assertTrue(result.succeeded());

        verify(vertx).undeploy(DEPLOYMENT_ID);
        verify(projectRequest).proceed(any(Registered.class));
    }
}