package com.getlipa.eventstore.core.event.seriesindex;

import com.getlipa.eventstore.core.event.EventMetadata;

public abstract class SeriesIndex {

    public static SeriesIndex first() {
        return at(0);
    }

    public static SeriesIndex after(EventMetadata eventMetadata) {
        if (eventMetadata == null) {
            return first();
        }
        return after(eventMetadata.getSeriesIndex());
    }

    public static SeriesIndex after(long index) {
        return at(index + 1);
    }

    public static SeriesIndex at(long index) {
        return new SpecificIndex(index);
    }

    public static SeriesIndex atAny() {
        return AnyIndex.INSTANCE;
    }

    public static SeriesIndex afterAny() {
        return AfterAnyIndex.INSTANCE;
    }

    public static SeriesIndex from(EventMetadata metadata) {
        if (metadata == null) {
            return first();
        }
        return after(metadata.getSeriesIndex());
    }

    public abstract long getValue();
}
