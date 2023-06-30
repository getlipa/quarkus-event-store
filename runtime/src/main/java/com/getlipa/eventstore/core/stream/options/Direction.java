package com.getlipa.eventstore.core.stream.options;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;

@Getter
@RequiredArgsConstructor
public enum Direction {

    FORWARD(Comparator.naturalOrder()),
    BACKWARD(Comparator.reverseOrder());

    private final Comparator<Long> positionComparator;
}
