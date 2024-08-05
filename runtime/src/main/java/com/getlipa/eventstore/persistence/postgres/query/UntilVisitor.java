package com.getlipa.eventstore.persistence.postgres.query;

import com.getlipa.eventstore.stream.reader.cursor.PositionCursor;
import com.getlipa.eventstore.stream.reader.cursor.LogIndexCursor;

public interface UntilVisitor {

    void visitUntil(LogIndexCursor cursor);

    void visitUntil(PositionCursor cursor);

}
