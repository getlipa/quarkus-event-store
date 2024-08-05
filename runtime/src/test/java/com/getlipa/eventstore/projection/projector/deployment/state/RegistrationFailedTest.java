package com.getlipa.eventstore.projection.projector.deployment.state;

import com.getlipa.eventstore.projection.projector.deployment.DeploymentId;
import com.getlipa.eventstore.projection.projector.commands.ApplyRequest;
import com.getlipa.eventstore.projection.projector.deployment.DeploymentManager;
import com.getlipa.eventstore.projection.projector.ProjectorId;
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

    private static final ProjectorId TARGET_ID = ProjectorId.createDefault("projection-name");

    private static final String INSTANCE_ID = "id";

    private static final DeploymentId PROJECTOR_ID = DeploymentId.create(
            TARGET_ID,
            INSTANCE_ID
    );

    private ProjectorState projectorState;

    @Mock
    DeploymentManager deploymentManager;

    @Mock
    Vertx vertx;

    @Mock
    Throwable cause;

    @Mock
    ApplyRequest applyRequest;

    @BeforeEach
    void setup() {
        doReturn(TARGET_ID).when(applyRequest).getProjectorId();
        doReturn(vertx).when(applyRequest).getVertx();
        doReturn(deploymentManager).when(applyRequest).getDeploymentManager();
        doReturn(Future.succeededFuture()).when(applyRequest).proceed(any());
        doReturn(Future.succeededFuture()).when(vertx).undeploy(anyString());
        doReturn(Future.succeededFuture(DeploymentId.create(TARGET_ID, "another-id")))
                .when(deploymentManager)
                .resolve(TARGET_ID);

        projectorState = ProjectorState.registrationFailed(PROJECTOR_ID, INSTANCE_ID, cause);
    }

    @Test
    void process() {
        final var result = projectorState.process(applyRequest);

        assertTrue(result.isComplete());
        assertTrue(result.succeeded());

        verify(vertx).undeploy(INSTANCE_ID);
        verify(applyRequest).proceed(any(Registered.class));
    }
}