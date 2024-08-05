package com.getlipa.eventstore.persistence.postgres.query;

import com.getlipa.eventstore.stream.reader.cursor.PositionCursor;
import com.getlipa.eventstore.stream.reader.cursor.LogIndexCursor;

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
