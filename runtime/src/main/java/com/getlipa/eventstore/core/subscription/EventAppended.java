package com.getlipa.eventstore.core.subscription;

import com.getlipa.eventstore.core.event.AnyEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class EventAppended {

    private final AnyEvent event;

    public static EventAppended create(AnyEvent event) {
        return new EventAppended(event);
    }

}
