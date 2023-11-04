package com.getlipa.eventstore.core.projection.projector.commands;

import com.getlipa.eventstore.core.event.AnyEvent;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProjectResponse {

    private final AnyEvent event;

    public static ProjectResponse create(AnyEvent event) {
        return new ProjectResponse(event);
    }

}
