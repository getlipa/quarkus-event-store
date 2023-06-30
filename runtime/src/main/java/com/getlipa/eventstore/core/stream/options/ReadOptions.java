package com.getlipa.eventstore.core.stream.options;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Accessors(fluent = true)
@Builder
public class ReadOptions {

    @Getter
    private final Direction direction;

    @Getter
    private final Cursor startAt;

    @Getter
    private final int limit;

    public static ReadOptionsBuilder builder() {
        return new ReadOptionsBuilder()
                .direction(Direction.FORWARD)
                .startAt(Cursor.streamStart())
                .limit(Integer.MAX_VALUE);
    }
}
