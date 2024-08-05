package com.getlipa.eventstore.event.selector;

import com.getlipa.eventstore.event.AnyEvent;
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

    @Override
    public Selector combineWith(Selector selector) {
        final var combined = Arrays.copyOf(matchers, matchers.length + 1, Selector[].class);
        combined[matchers.length] = selector;
        return Selector.all(combined);
    }

    @Override
    public String toString() {
        return Arrays.toString(matchers);
    }
}
