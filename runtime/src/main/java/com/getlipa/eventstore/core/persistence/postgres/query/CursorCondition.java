package com.getlipa.eventstore.core.persistence.postgres.query;

import com.getlipa.eventstore.core.stream.reader.cursor.PositionCursor;
import com.getlipa.eventstore.core.stream.reader.cursor.LogIndexCursor;

public class CursorCondition extends Condition {

    private String column;

    private long value;

    @Override
    public void visitStartAt(LogIndexCursor cursor) {

    }

    @Override
    public void visitStartAt(PositionCursor cursor) {

    }
}
