package com.getlipa.eventstore.core.persistence.postgres.query;

import com.getlipa.eventstore.core.stream.options.Cursor;
import com.getlipa.eventstore.core.stream.options.PositionCursor;
import com.getlipa.eventstore.core.stream.options.SeriesIndexCursor;

public abstract class Condition implements Cursor.Visitor {

    @Override
    public void visit(SeriesIndexCursor cursor) {

    }

    @Override
    public void visit(PositionCursor cursor) {

    }
}
