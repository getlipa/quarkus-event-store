package com.getlipa.eventstore.core.event.seriesindex;

import lombok.ToString;

@ToString(includeFieldNames = false)
final class AfterAnyIndex extends SeriesIndex {

    static final long VALUE = -2;

    static final AfterAnyIndex INSTANCE = new AfterAnyIndex();

    @Override
    public long getValue() {
        return VALUE;
    }
}
