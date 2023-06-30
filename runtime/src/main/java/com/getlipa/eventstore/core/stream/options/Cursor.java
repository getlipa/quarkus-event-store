package com.getlipa.eventstore.core.stream.options;

import com.getlipa.eventstore.core.event.Event;

public abstract class Cursor implements Comparable<Event<?>> {

    public static Cursor event(Event<?> event) {
        return position(event.getPosition());
    }

    public static Cursor streamStart() {
        return position(0);
    }

    public static Cursor streamEnd() {
        return position(Long.MAX_VALUE);
    }

    public static Cursor seriesIndex(long seriesIndex) {
        return new SeriesIndexCursor(seriesIndex);
    }

    public static Cursor position(long position) {
        return new PositionCursor(position);
    }

    public abstract void accept(Visitor visitor);

    public interface Visitor {

        void visit(SeriesIndexCursor cursor);

        void visit(PositionCursor cursor);

    }
}
