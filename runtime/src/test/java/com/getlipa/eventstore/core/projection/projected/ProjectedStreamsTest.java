package com.getlipa.eventstore.core.projection.projected;

import com.getlipa.eventstore.core.projection.ProjectionMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProjectedStreamsTest {

    private static final String TYPE = "type";

    private static final String ID = "id";

    @Mock
    ProjectedStream.Factory factory;

    @Mock
    ProjectionMetadata projectionMetadata;

    @Mock
    ProjectedStream<Object> projectedStream;

    ProjectedStreams<Object> projectedStreams;

    @BeforeEach
    void setUp() {
        doReturn(TYPE).when(projectionMetadata).getName();
        doReturn(projectedStream).when(factory).create(TYPE, ID);

        projectedStreams = new ProjectedStreams<>(projectionMetadata, factory);
    }

    @Test
    void get() {
        final var result = projectedStreams.get(ID);

        assertSame(projectedStream, result);
        verify(factory).create(TYPE, ID);
    }
}