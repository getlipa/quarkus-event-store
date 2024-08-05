package com.getlipa.eventstore.stream.reader.cursor;

import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.persistence.postgres.query.StartAtVisitor;
import com.getlipa.eventstore.persistence.postgres.query.UntilVisitor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PositionCursor extends Cursor {

    private final long position;

    @Override
    public int compareTo(AnyEvent event) {
        return Long.compare(position, event.getPosition());
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
