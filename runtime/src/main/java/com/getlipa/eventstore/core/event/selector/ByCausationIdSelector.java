package com.getlipa.eventstore.core.event.selector;

import com.getlipa.eventstore.core.event.AnyEvent;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class ByCausationIdSelector implements Selector {

    private final UUID causationId;

    public ByCausationIdSelector(String causationId) {
        this(UUID.fromString(causationId));
    }

    @Override
    public boolean matches(AnyEvent event) {
        return causationId.equals(event.getCausationId());
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
