package com.getlipa.eventstore.core.event.seriesindex;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString(includeFieldNames = false)
final class SpecificIndex extends SeriesIndex {

    @Getter
    final long value;

}
