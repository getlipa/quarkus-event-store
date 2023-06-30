package com.getlipa.eventstore.core.stream.options;

import com.getlipa.eventstore.core.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PositionCursor extends Cursor {

    private final long position;

    @Override
    public int compareTo(Event<?> event) {
        return Long.compare(position, event.getPosition());
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
