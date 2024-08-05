package com.getlipa.eventstore.projection.projector.deployment.state;

import com.getlipa.eventstore.projection.projector.deployment.DeploymentId;
import com.getlipa.eventstore.projection.projector.commands.ApplyRequest;
import com.getlipa.eventstore.projection.projector.deployment.DeploymentManager;
import com.getlipa.eventstore.projection.projector.ProjectorId;
import io.vertx.core.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeployedTest {

    private static final ProjectorId TARGET_ID = ProjectorId.createDefault("projection-name");

    private static final String INSTANCE_ID = "id";

    private static final DeploymentId PROJECTOR_ID = DeploymentId.create(TARGET_ID, INSTANCE_ID);

    private ProjectorState projectorState;

    @Mock
    ApplyRequest applyRequest;

    @Mock
    DeploymentManager deploymentManager;

    @BeforeEach
    void setup() {
        doReturn(deploymentManager).when(applyRequest).getDeploymentManager();
        doReturn(TARGET_ID).when(applyRequest).getProjectorId();
        doReturn(Future.succeededFuture()).when(applyRequest).proceed(any());

        projectorState = ProjectorState.deployed(INSTANCE_ID);
    }

    @Test
    void process() {
        doReturn(Future.succeededFuture()).when(deploymentManager).register(PROJECTOR_ID);

        projectorState.process(applyRequest);

        verify(deploymentManager).register(PROJECTOR_ID);
        verify(applyRequest).proceed(any(Registered.class));
    }

    @Test
    void process_registrationFailed() {
        doReturn(Future.failedFuture("failed")).when(deploymentManager).register(PROJECTOR_ID);

        projectorState.process(applyRequest);

        verify(applyRequest).proceed(any(RegistrationFailed.class));
    }
}