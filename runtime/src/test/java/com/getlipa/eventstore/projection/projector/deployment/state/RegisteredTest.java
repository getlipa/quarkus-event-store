package com.getlipa.eventstore.projection.projector.deployment.state;


import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.projection.projector.deployment.ProjectorDeployment;
import com.getlipa.eventstore.projection.projector.deployment.DeploymentId;
import com.getlipa.eventstore.projection.projector.commands.ApplyRequest;
import com.getlipa.eventstore.projection.projector.ProjectorId;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisteredTest {

    private static final DeploymentId PROJECTOR_ID = DeploymentId.create(
            ProjectorId.createDefault("projection-name"),
            0L
    );

    @Mock
    private Vertx vertx;

    @Mock
    private EventBus eventBus;

    @Mock
    private AnyEvent event;

    @Mock
    private ApplyRequest applyRequest;

    private ProjectorState registered;

    @BeforeEach
    public void setup() {
        doReturn(eventBus).when(vertx).eventBus();
        doReturn(event).when(applyRequest).getEvent();
        doReturn(vertx).when(applyRequest).getVertx();

        registered = ProjectorState.registered(PROJECTOR_ID);
    }

    @Test
    public void process() {
        final var response = mock(Message.class);
        doReturn(Future.succeededFuture(response))
                .when(eventBus)
                .request(anyString(), any(), any(DeliveryOptions.class));

        registered.process(applyRequest);

        verify(eventBus).request(
                PROJECTOR_ID.toString(),
                event,
                ProjectorDeployment.DELIVERY_OPTIONS
        );
    }

    @Test
    public void process_failed() {
        doReturn(Future.succeededFuture()).when(applyRequest).proceed(any(Unregistered.class));
        doReturn(Future.failedFuture("failed"))
                .when(eventBus)
                .request(anyString(), any(), any(DeliveryOptions.class));

        registered.process(applyRequest);

        verify(applyRequest).proceed(any(Unregistered.class));
    }
}