package com.getlipa.eventstore.core.projection.projector.instance.state;


import com.getlipa.eventstore.core.event.AnyEvent;
import com.getlipa.eventstore.core.projection.projector.Projector;
import com.getlipa.eventstore.core.projection.projector.commands.ProjectRequest;
import com.getlipa.eventstore.core.projection.trgt.ProjectionTarget;
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

    private static final Projector.Id PROJECTOR_ID = Projector.Id.create(
            ProjectionTarget.Id.create("projector-type", "projector-name"),
            0L
    );

    @Mock
    private Vertx vertx;

    @Mock
    private EventBus eventBus;

    @Mock
    private AnyEvent event;

    @Mock
    private ProjectRequest projectRequest;

    private ProjectorState registered;

    @BeforeEach
    public void setup() {
        doReturn(eventBus).when(vertx).eventBus();
        doReturn(event).when(projectRequest).getEvent();
        doReturn(vertx).when(projectRequest).getVertx();

        registered = ProjectorState.registered(PROJECTOR_ID);
    }

    @Test
    public void process() {
        final var response = mock(Message.class);
        doReturn(Future.succeededFuture(response))
                .when(eventBus)
                .request(anyString(), any(), any(DeliveryOptions.class));

        registered.process(projectRequest);

        verify(eventBus).request(
                PROJECTOR_ID.toString(),
                event,
                Projector.DELIVERY_OPTIONS
        );
    }

    @Test
    public void process_failed() {
        doReturn(Future.succeededFuture()).when(projectRequest).proceed(any(Unregistered.class));
        doReturn(Future.failedFuture("failed"))
                .when(eventBus)
                .request(anyString(), any(), any(DeliveryOptions.class));

        registered.process(projectRequest);

        verify(projectRequest).proceed(any(Unregistered.class));
    }
}