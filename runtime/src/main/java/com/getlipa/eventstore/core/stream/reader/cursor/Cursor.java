package com.getlipa.eventstore.core.stream.reader.cursor;

import com.getlipa.eventstore.core.event.AnyEvent;
import com.getlipa.eventstore.core.persistence.postgres.query.StartAtVisitor;
import com.getlipa.eventstore.core.persistence.postgres.query.UntilVisitor;
import com.getlipa.eventstore.core.stream.reader.Direction;

public abstract class Cursor implements Comparable<AnyEvent> {

    public static Cursor position(AnyEvent event) {
        return position(event.getPosition());
    }

    public static Cursor streamStart() {
        return position(0);
    }

    public static Cursor streamEnd() {
        return position(Long.MAX_VALUE);
    }

    public static Cursor logIndex(long logIndex) {
        return new LogIndexCursor(logIndex);
    }

    public static Cursor position(long position) {
        return new PositionCursor(position);
    }

    public boolean isBefore(AnyEvent event, Direction direction) {
        return direction.getPositionComparator().compare((long) compareTo(event), 0L) < 0;
    }

    public boolean isAfter(AnyEvent event, Direction direction) {
        return direction.getPositionComparator().compare((long) compareTo(event), 0L) > 0;
    }

    public boolean pointsTo(AnyEvent event) {
        return compareTo(event) == 0;
    }

    public abstract void accept(StartAtVisitor visitor);

    public abstract void accept(UntilVisitor visitor);

}
