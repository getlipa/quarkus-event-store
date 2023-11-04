package com.getlipa.eventstore.core.projection.checkpointing;

import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.proto.Payload;
import com.getlipa.eventstore.subscriptions.Projections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class ProjectionProgressTest {

    ProjectionProgress projectionProgress;

    @BeforeEach
    public void setup() {
        projectionProgress = new ProjectionProgress();
    }

    @Test
    public void testInitialState() {
        assertEquals(0, projectionProgress.getCatchUpTip());
        assertEquals(Long.MAX_VALUE, projectionProgress.getCatchUpTarget());
        assertNull(projectionProgress.getCatchUpProcessId());
    }

    @Test
    public void testStaleCheckpoint() {
        projectionProgress.onCheckpointReached(createCheckpointEvent("unknown-source"));

        assertEquals(0, projectionProgress.getCatchUpTip());
        assertEquals(Long.MAX_VALUE, projectionProgress.getCatchUpTarget());
        assertNull(projectionProgress.getCatchUpProcessId());
    }



    Event<Projections.CheckpointReached> createCheckpointEvent(String id) {
        final var event = mock(Event.class);
        doReturn(UUID.nameUUIDFromBytes(id.getBytes(StandardCharsets.UTF_8))).when(event).getCausationId();
        doReturn(Payload.create(Projections.CheckpointReached.newBuilder().build())).when(event).getPayload();
        return event;
    }
}