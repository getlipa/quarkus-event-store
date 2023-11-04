package com.getlipa.eventstore.core.stream.reader;

import com.getlipa.eventstore.core.stream.reader.cursor.Cursor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.function.Consumer;

@Getter
@RequiredArgsConstructor
public enum Direction {

    FORWARD(
            Comparator.naturalOrder(),
            readOptions -> readOptions.from(Cursor.streamStart()).until(Cursor.streamEnd())
    ),
    BACKWARD(
            Comparator.reverseOrder(),
            readOptions -> readOptions.from(Cursor.streamEnd()).until(Cursor.streamStart())
    );

    private final Comparator<Long> positionComparator;

    private final Consumer<ReadOptions.ReadOptionsBuilder> readOptionsInitializer;

    public ReadOptions.ReadOptionsBuilder readOptions() {
        final var builder = ReadOptions.builder().direction(this);
        readOptionsInitializer.accept(builder);
        return builder;
    }
}
