package com.getlipa.eventstore.core.event.selector;

import com.getlipa.eventstore.core.event.AnyEvent;
import io.quarkus.runtime.annotations.RecordableConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor(onConstructor = @__(@RecordableConstructor))
public class CompositeSelector implements Selector {

    private final Selector[] matchers;


    @Override
    public boolean matches(AnyEvent event) {
        return Arrays.stream(matchers)
                .allMatch(matcher -> matcher.matches(event));
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void eachAccept(Visitor visitor) {
        for (final var matcher : matchers) {
            matcher.accept(visitor);
        }
    }
}
