package com.getlipa.eventstore.core.projection.projected;

import com.getlipa.eventstore.core.event.AnyEvent;
import com.getlipa.eventstore.core.event.EphemeralEvent;
import com.getlipa.eventstore.core.event.logindex.LogIndex;
import com.getlipa.eventstore.core.projection.trgt.ProjectionTarget;
import com.getlipa.eventstore.core.stream.AppendableStream;
import io.vertx.core.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectedLogTest {

    private static final long EVENT_TIP_LOG_INDEX = 3L;

    @Mock
    AppendableStream appendableStream;

    @Mock
    ProjectionTarget<Object> projectionTarget;

    ProjectedLog<Object> projectedLog;

    @BeforeEach
    void beforeEach() {
        projectedLog = ProjectedLog.create(projectionTarget, appendableStream);

        final var eventTip = Mockito.mock(AnyEvent.class);
        doReturn(EVENT_TIP_LOG_INDEX).when(eventTip).getLogIndex();
        doReturn(eventTip).when(projectionTarget).getEventTip();
    }

    @Test
    void getRevision() {
        final var revision = projectedLog.getRevision();
        assertEquals(EVENT_TIP_LOG_INDEX, revision);
    }

    @Test
    void append() {
        final var persisted = mock(AnyEvent.class);

        doReturn(Future.succeededFuture()).when(projectionTarget).initialized();
        doReturn(Future.succeededFuture(persisted)).when(appendableStream).append(any(), any());
        doReturn(Future.succeededFuture()).when(projectionTarget).apply(any());

        final var event = Mockito.mock(EphemeralEvent.class);
        projectedLog.append(event);
        verify(appendableStream).append(LogIndex.at(EVENT_TIP_LOG_INDEX + 1), event);
        verify(projectionTarget).apply(persisted);
    }
}