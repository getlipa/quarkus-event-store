package com.getlipa.eventstore.persistence.postgres.query;

import com.getlipa.eventstore.stream.reader.cursor.PositionCursor;
import com.getlipa.eventstore.stream.reader.cursor.LogIndexCursor;

public abstract class Condition implements StartAtVisitor {

    @Override
    public void visitStartAt(LogIndexCursor cursor) {

    }

    @Override
    public void visitStartAt(PositionCursor cursor) {

    }
}
