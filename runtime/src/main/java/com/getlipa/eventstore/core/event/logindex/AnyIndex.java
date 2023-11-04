package com.getlipa.eventstore.core.event.logindex;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(includeFieldNames = false)
final class AnyIndex extends LogIndex {

    static final long VALUE = -1;

    static final AnyIndex INSTANCE = new AnyIndex();

    @Override
    public void validate(long index) {
    }

    @Override
    public long getValue() {
        return VALUE;
    }
}
