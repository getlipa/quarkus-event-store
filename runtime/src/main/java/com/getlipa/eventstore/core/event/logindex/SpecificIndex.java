package com.getlipa.eventstore.core.event.logindex;

import com.getlipa.eventstore.core.persistence.exception.InvalidIndexException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString(includeFieldNames = false)
@EqualsAndHashCode(callSuper = true)
@Getter
final class SpecificIndex extends LogIndex {

    final long value;

    @Override
    public void validate(long index) throws InvalidIndexException {
        if (value != index) {
            throw InvalidIndexException.nonConsecutiveIndex(null);
        };
    }
}
