package com.getlipa.eventstore.projection.projector;

import com.getlipa.eventstore.identifier.Id;
import com.getlipa.eventstore.projection.projector.scope.ProjectorScopeContext;
import com.getlipa.eventstore.projection.projector.scope.ProjectorScoped;
import jakarta.enterprise.inject.Produces;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class ProjectorId {

    private static final char SEPARATOR = ':';

    private final String projectionName;

    private final Id id;

    public static ProjectorId createDefault(String type) {
        return create(type, Id.nil());
    }

    public static ProjectorId fromVerticleId(@NonNull String id) {
        final var separatorIndex = id.indexOf(SEPARATOR);
        if (separatorIndex < 0) {
            throw new IllegalArgumentException(
                    String.format("Invalid id, expected <type>%s<name> but got '%s'", SEPARATOR, id)
            );
        }
        return create(id.substring(0, separatorIndex), id.substring(separatorIndex + 1));
    }

    public static ProjectorId create(String projectionName, String name) {
        return create(projectionName, Id.from(UUID.fromString(name)));
    }

    public static ProjectorId create(String projectionName, Id id) {
        return new ProjectorId(projectionName, id);
    }

    public String toVerticleId() {
        return projectionName + SEPARATOR + id.toUuid().toString();
    }

    @Override
    public String toString() {
        return String.format("%s:%s", projectionName, id.toString());
    }

    public static class Producer {

        @Produces
        @ProjectorScoped
        public ProjectorId produce() {
            return ProjectorScopeContext.current().getId();
        }
    }
}
