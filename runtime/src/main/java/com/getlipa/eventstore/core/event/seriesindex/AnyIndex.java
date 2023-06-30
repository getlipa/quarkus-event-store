package com.getlipa.eventstore.core.event.seriesindex;

import lombok.ToString;

@ToString(includeFieldNames = false)
final class AnyIndex extends SeriesIndex {

    static final long VALUE = -1;

    static final AnyIndex INSTANCE = new AnyIndex();

    @Override
    public long getValue() {
        return VALUE;
    }
}
