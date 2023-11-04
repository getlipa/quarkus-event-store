package com.getlipa.eventstore.core.projection.projected;

import com.getlipa.eventstore.core.projection.ProjectionMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProjectedLogsTest {

    private static final String TYPE = "type";

    private static final String ID = "id";

    @Mock
    ProjectedLog.Factory factory;

    @Mock
    ProjectionMetadata projectionMetadata;

    @Mock
    ProjectedLog<Object> projectedLog;

    ProjectedLogs<Object> projectedLogs;

    @BeforeEach
    void setUp() {
        doReturn(TYPE).when(projectionMetadata).getName();
        doReturn(projectedLog).when(factory).create(TYPE, ID);

        projectedLogs = new ProjectedLogs<>(projectionMetadata, factory);
    }

    @Test
    void get() {
        final var result = projectedLogs.get(ID);

        assertSame(projectedLog, result);
        verify(factory).create(TYPE, ID);
    }
}