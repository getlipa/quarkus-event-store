package com.getlipa.eventstore.projection.projector.commands;

import com.getlipa.eventstore.event.AnyEvent;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ApplyResponse {

    private final AnyEvent event;

    public static ApplyResponse create(AnyEvent event) {
        return new ApplyResponse(event);
    }

}
