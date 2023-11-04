package com.getlipa.eventstore.core.event.logindex;

import com.getlipa.eventstore.core.persistence.exception.InvalidIndexException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(includeFieldNames = false)
final class AfterAnyIndex extends LogIndex {

    static final long VALUE = -2;

    static final AfterAnyIndex INSTANCE = new AfterAnyIndex();

    @Override
    public void validate(long index) throws InvalidIndexException {
        if (index == 0) {
            throw InvalidIndexException.nonConsecutiveIndex(null);
        }
    }

    @Override
    public long getValue() {
        return VALUE;
    }
}
