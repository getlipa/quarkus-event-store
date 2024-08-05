package com.getlipa.eventstore.projection.projector.deployment;

import com.getlipa.eventstore.projection.projector.ProjectorId;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class DeploymentId {

    private final ProjectorId projectorId;

    private final long instanceId;

    public static DeploymentId create(ProjectorId projectorId, String deploymentId) {
        return create(projectorId, Integer.toUnsignedLong(deploymentId.hashCode()));
    }

    public static DeploymentId create(ProjectorId projectorId, long instanceId) {
        return new DeploymentId(projectorId, instanceId);
    }

    public String toString() {
        return String.format("%s+%s", projectorId, instanceId);
    }
}
