package com.getlipa.eventstore.core.stream.options;

import com.getlipa.eventstore.core.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SeriesIndexCursor extends Cursor {

    private final long seriesIndex;

    @Override
    public int compareTo(Event<?> event) {
        return Long.compare(seriesIndex, event.getSeriesIndex());
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
