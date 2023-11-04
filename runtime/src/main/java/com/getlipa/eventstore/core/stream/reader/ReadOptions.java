package com.getlipa.eventstore.core.stream.reader;

import com.getlipa.eventstore.core.stream.reader.cursor.Cursor;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Accessors(fluent = true)
@Builder
@Getter
public class ReadOptions {

    public static final int DEFAULT_PAGE_SIZE = 1000;

    public static final ReadOptions DEFAULT = ReadOptions.builder().build();

    private final Direction direction;

    private final Cursor from;

    private final Cursor until;

    private final int limit;

    @Getter(AccessLevel.PACKAGE)
    private final int pageSize;

    public static ReadOptionsBuilder from(ReadOptions readOptions) {
        return builder()
                .direction(readOptions.direction)
                .from(readOptions.from)
                .until(readOptions.until)
                .limit(readOptions.limit)
                .pageSize(readOptions.pageSize);
    }

    public static ReadOptionsBuilder builder() {
        return new ReadOptionsBuilder()
                .direction(Direction.FORWARD)
                .from(Cursor.streamStart())
                .until(Cursor.streamEnd())
                .limit(Integer.MAX_VALUE)
                .pageSize(DEFAULT_PAGE_SIZE);
    }
}
