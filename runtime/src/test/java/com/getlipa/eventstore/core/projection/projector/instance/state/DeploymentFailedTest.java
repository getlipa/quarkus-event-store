package com.getlipa.eventstore.core.projection.projector.instance.state;

import com.getlipa.eventstore.core.projection.projector.commands.ProjectRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DeploymentFailedTest {

    private ProjectorState projectorState;

    @Mock
    Throwable cause;

    @Mock
    ProjectRequest projectRequest;

    @BeforeEach
    void setup() {
        projectorState = ProjectorState.deploymentFailed(cause);
    }

    @Test
    void process() {
        final var result = projectorState.process(projectRequest);

        assertTrue(result.isComplete());
        assertTrue(result.failed());
        result.onFailure(failure -> assertSame(cause, failure));
    }
}