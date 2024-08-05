package com.getlipa.eventstore.event.selector;

import com.getlipa.eventstore.event.AnyEvent;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class AllSelector implements Selector {

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean matches(AnyEvent event) {
        return true;
    }
}
