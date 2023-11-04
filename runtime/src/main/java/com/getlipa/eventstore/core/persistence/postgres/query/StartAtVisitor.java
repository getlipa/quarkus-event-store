package com.getlipa.eventstore.core.persistence.postgres.query;

import com.getlipa.eventstore.core.stream.reader.cursor.PositionCursor;
import com.getlipa.eventstore.core.stream.reader.cursor.LogIndexCursor;

public interface StartAtVisitor {

    void visitStartAt(LogIndexCursor cursor);

    void visitStartAt(PositionCursor cursor);

}
