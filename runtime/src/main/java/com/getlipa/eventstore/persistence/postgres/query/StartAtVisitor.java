package com.getlipa.eventstore.persistence.postgres.query;

import com.getlipa.eventstore.stream.reader.cursor.PositionCursor;
import com.getlipa.eventstore.stream.reader.cursor.LogIndexCursor;

public interface StartAtVisitor {

    void visitStartAt(LogIndexCursor cursor);

    void visitStartAt(PositionCursor cursor);

}
