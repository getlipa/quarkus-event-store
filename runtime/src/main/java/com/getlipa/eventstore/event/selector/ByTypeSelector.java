package com.getlipa.eventstore.event.selector;

import com.getlipa.eventstore.event.AnyEvent;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class ByTypeSelector implements Selector {

    private final String type;

    @Override
    public boolean matches(AnyEvent event) {
        return false; // FIXME!!
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return Selector.toString("type", type);
    }
}
