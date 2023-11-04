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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeployedTest {

    private static final ProjectionTarget.Id TARGET_ID = ProjectionTarget.Id.create("projector-type", "projector-name");

    private static final String DEPLOYMENT_ID = "deployment-id";

    private static final Projector.Id PROJECTOR_ID = Projector.Id.create(TARGET_ID, DEPLOYMENT_ID);

    private ProjectorState projectorState;

    @Mock
    ProjectRequest projectRequest;

    @Mock
    InstanceManager instanceManager;

    @BeforeEach
    void setup() {
        doReturn(instanceManager).when(projectRequest).getInstanceManager();
        doReturn(TARGET_ID).when(projectRequest).getTargetId();
        doReturn(Future.succeededFuture()).when(projectRequest).proceed(any());

        projectorState = ProjectorState.deployed(DEPLOYMENT_ID);
    }

    @Test
    void process() {
        doReturn(Future.succeededFuture()).when(instanceManager).register(PROJECTOR_ID);

        projectorState.process(projectRequest);

        verify(instanceManager).register(PROJECTOR_ID);
        verify(projectRequest).proceed(any(Registered.class));
    }

    @Test
    void process_registrationFailed() {
        doReturn(Future.failedFuture("failed")).when(instanceManager).register(PROJECTOR_ID);

        projectorState.process(projectRequest);

        verify(projectRequest).proceed(any(RegistrationFailed.class));
    }
}