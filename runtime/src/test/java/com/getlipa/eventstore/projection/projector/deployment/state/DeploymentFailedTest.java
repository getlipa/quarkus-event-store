package com.getlipa.eventstore.projection.projector.deployment.state;

import com.getlipa.eventstore.projection.projector.commands.ApplyRequest;
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
    ApplyRequest applyRequest;

    @BeforeEach
    void setup() {
        projectorState = ProjectorState.deploymentFailed(cause);
    }

    @Test
    void process() {
        final var result = projectorState.process(applyRequest);

        assertTrue(result.isComplete());
        assertTrue(result.failed());
        result.onFailure(failure -> assertSame(cause, failure));
    }
}