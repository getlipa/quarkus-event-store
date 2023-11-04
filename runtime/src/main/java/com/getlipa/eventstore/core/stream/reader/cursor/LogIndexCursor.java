package com.getlipa.eventstore.core.stream.reader.cursor;

import com.getlipa.eventstore.core.event.AnyEvent;
import com.getlipa.eventstore.core.persistence.postgres.query.StartAtVisitor;
import com.getlipa.eventstore.core.persistence.postgres.query.UntilVisitor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LogIndexCursor extends Cursor {

    private final long logIndex;

    @Override
    public int compareTo(AnyEvent event) {
        return Long.compare(logIndex, event.getLogIndex());
    }

    @Override
    public void accept(StartAtVisitor visitor) {
        visitor.visitStartAt(this);
    }

    @Override
    public void accept(UntilVisitor visitor) {
        visitor.visitUntil(this);
    }
}
