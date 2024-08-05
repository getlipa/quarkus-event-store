package com.getlipa.eventstore.event.selector;

import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.identifier.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class ByCausationIdSelector implements Selector {

    private final Id causationId;

    public ByCausationIdSelector(String causationId) {
        this(Id.derive(causationId));
    }

    @Override
    public boolean matches(AnyEvent event) {
        return causationId.equals(event.getCausationId());
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return Selector.toString("causationId", causationId.toString());
    }
}
