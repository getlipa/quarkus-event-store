package com.getlipa.eventstore.projection.projector.deployment.state;


import com.getlipa.eventstore.projection.projector.commands.ApplyRequest;
import com.getlipa.eventstore.projection.projector.ProjectorId;
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

    private static final ProjectorId TARGET_ID = ProjectorId.createDefault("projection-name");

    private static final String INSTANCE_ID = "id";

    @Mock
    private Vertx vertx;

    @Mock
    private ApplyRequest applyRequest;

    private Unregistered state;

    @BeforeEach
    public void setup() {
        doReturn(TARGET_ID).when(applyRequest).getProjectorId();
        doReturn(vertx).when(applyRequest).getVertx();
        doReturn(Future.succeededFuture()).when(applyRequest).proceed(any());

        state = new Unregistered();
    }

    @Test
    void process() {
        doReturn(Future.succeededFuture(INSTANCE_ID)).when(vertx).deployVerticle(anyString());

        state.process(applyRequest);

        verify(applyRequest).proceed(any(Deployed.class));
    }

    @Test
    void process_deploymentFailed() {
        doReturn(Future.failedFuture("failed")).when(vertx).deployVerticle(anyString());

        state.process(applyRequest);

        verify(applyRequest).proceed(any(DeploymentFailed.class));
    }
}