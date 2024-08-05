package com.getlipa.eventstore.stream.reader.cursor;

import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.persistence.postgres.query.StartAtVisitor;
import com.getlipa.eventstore.persistence.postgres.query.UntilVisitor;
import com.getlipa.eventstore.stream.reader.Direction;

class StreamStart extends Cursor {

    @Override
    public void accept(StartAtVisitor visitor) {

    }

    @Override
    public void accept(UntilVisitor visitor) {

    }

    @Override
    public SpecificCursor toSpecificCursor(Direction direction) {
        return null;
    }

    @Override
    public int compareTo(AnyEvent o) {
        return 0;
    }
}
