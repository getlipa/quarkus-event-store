package com.getlipa.eventstore.core.projection.projected;

import com.getlipa.eventstore.core.event.AnyEvent;
import com.getlipa.eventstore.core.projection.trgt.ProjectionTarget;
import io.vertx.core.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ProjectedStreamTest {

    @Mock
    ProjectionTarget<Object> projectionTarget;

    @Mock
    Object target;

    ProjectedStream<Object> projectedStream;

    @BeforeEach
    void beforeEach() {
        projectedStream = ProjectedStream.create(projectionTarget);
    }

    @Test
    void get() {
        Mockito.doReturn(Future.succeededFuture(target)).when(projectionTarget).initialized();

        final var result = projectedStream.get();

        assertTrue(result.isComplete());
        result.onSuccess(actual -> assertSame(target, actual));
    }

    @Test
    void getRevision() {
        final var eventTip = Mockito.mock(AnyEvent.class);
        final var expectedRevision = 3L;

        doReturn(expectedRevision).when(eventTip).getPosition();
        doReturn(eventTip).when(projectionTarget).getEventTip();

        final var revision = projectedStream.getRevision();
        assertEquals(expectedRevision, revision);
    }

    @Test
    void getRevision_emptyStream() {
        final var revision = projectedStream.getRevision();
        assertEquals(-1, revision);
    }
}