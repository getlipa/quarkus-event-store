package com.getlipa.eventstore.core.actor.cdi;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class ActorId {

    private static final char SEPARATOR = ':';

    private final String type;

    private final String name;

    public static ActorId createDefault(String type) {
        return create(type, "_default_");
    }

    public static ActorId create(@NonNull String id) {
        final var separatorIndex = id.indexOf(SEPARATOR);
        if (separatorIndex < 0) {
            throw new IllegalArgumentException(
                    String.format("Invalid id, expected <type>%s<name> but got '%s'", SEPARATOR, id)
            );
        }
        return create(id.substring(0, separatorIndex), id.substring(separatorIndex + 1));
    }

    public static ActorId create(String type, String name) {
        return new ActorId(type, name);
    }

    @Override
    public String toString() {
        return type + SEPARATOR + name;
    }
}
