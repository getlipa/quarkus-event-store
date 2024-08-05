package com.getlipa.eventstore.projection.catchup;

import com.getlipa.eventstore.Jobs;
import com.getlipa.eventstore.event.Event;
import com.getlipa.eventstore.event.payload.Payload;
import com.getlipa.eventstore.projection.catchup.ProjectionProgress;
import com.getlipa.eventstore.subscriptions.Projections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        assertNull(projectionProgress.getCatchUpJobId());
    }

    @Test
    public void testStaleCheckpoint() {
        projectionProgress.onCheckpointReached(createCheckpointEvent("unknown-source"));

        assertEquals(0, projectionProgress.getCatchUpTip());
        assertEquals(Long.MAX_VALUE, projectionProgress.getCatchUpTarget());
        assertNull(projectionProgress.getCatchUpJobId());
    }



    Event<Jobs.CheckpointReached> createCheckpointEvent(String id) {
        final var event = mock(Event.class);
        //doReturn(UUID.nameUUIDFromBytes(id.getBytes(StandardCharsets.UTF_8))).when(event).getCausationUuid();
        doReturn(Payload.create(Projections.CheckpointReached.newBuilder().build())).when(event).getPayload();
        return event;
    }
}