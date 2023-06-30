package com.getlipa.eventstore.core.persistence.postgres.query;

import com.getlipa.eventstore.core.stream.options.PositionCursor;
import com.getlipa.eventstore.core.stream.options.SeriesIndexCursor;

public class CursorCondition extends Condition {

    private String column;

    private long value;

    @Override
    public void visit(SeriesIndexCursor cursor) {

    }

    @Override
    public void visit(PositionCursor cursor) {

    }
}
