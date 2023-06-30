package com.getlipa.eventstore.core.persistence.postgres.query;

import com.getlipa.eventstore.core.stream.options.Direction;
import io.quarkus.panache.common.Sort;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Sorting {

    FORWARD("<", Sort.ascending("position")),
    BACKWARD(">", Sort.descending("position"));

    private final String operator;

    private final Sort sort;

    public static Sorting valueOf(Direction direction) {
        if (direction == Direction.FORWARD) {
            return FORWARD;
        }
        return BACKWARD;
    }

}
