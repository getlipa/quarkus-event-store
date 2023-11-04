package com.getlipa.eventstore.core.projection.projector.instance.state;


import com.getlipa.eventstore.core.projection.projector.commands.ProjectRequest;
import com.getlipa.eventstore.core.projection.trgt.ProjectionTarget;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnregisteredTest {

    private static final ProjectionTarget.Id TARGET_ID = ProjectionTarget.Id.create("projector-type", "projector-name");

    private static final String DEPLOYMENT_ID = "deployment-id";

    @Mock
    private Vertx vertx;

    @Mock
    private ProjectRequest projectRequest;

    private Unregistered state;

    @BeforeEach
    public void setup() {
        doReturn(TARGET_ID).when(projectRequest).getTargetId();
        doReturn(vertx).when(projectRequest).getVertx();
        doReturn(Future.succeededFuture()).when(projectRequest).proceed(any());

        state = new Unregistered();
    }

    @Test
    void process() {
        doReturn(Future.succeededFuture(DEPLOYMENT_ID)).when(vertx).deployVerticle(anyString());

        state.process(projectRequest);

        verify(projectRequest).proceed(any(Deployed.class));
    }

    @Test
    void process_deploymentFailed() {
        doReturn(Future.failedFuture("failed")).when(vertx).deployVerticle(anyString());

        state.process(projectRequest);

        verify(projectRequest).proceed(any(DeploymentFailed.class));
    }
}