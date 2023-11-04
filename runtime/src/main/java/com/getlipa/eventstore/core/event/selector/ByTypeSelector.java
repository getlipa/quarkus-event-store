package com.getlipa.eventstore.core.event.selector;

import com.getlipa.eventstore.core.event.AnyEvent;
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
}
