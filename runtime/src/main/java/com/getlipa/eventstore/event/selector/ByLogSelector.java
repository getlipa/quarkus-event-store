package com.getlipa.eventstore.event.selector;

import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.event.Events;
import com.getlipa.eventstore.identifier.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class ByLogSelector implements Selector {

    private final String context;

    private final Id logId;

    @Override
    public boolean matches(AnyEvent event) {
        return context.equals(event.getLogContext()) && logId.equals(event.getLogId());
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public static ByLogSelector from(Selector ...selectors) {
        return from(Selector.all(selectors));
    }

    public static ByLogSelector from(Selector selector) {
        final var extractor = new Extractor();
        selector.accept(extractor);
        return extractor.extract();
    }

    @Override
    public String toString() {
        return Selector.toString("log", String.format("%s-%s", context, logId.toString()));
    }

    @NoArgsConstructor
    private static class Extractor implements Selector.IgnorantVisitor {

        private ByLogIdSelector byLogIdSelector;

        private ByContextSelector byContextSelector;

        @Override
        public void visit(ByLogIdSelector byLogIdSelector) {
            this.byLogIdSelector = byLogIdSelector;
        }

        @Override
        public void visit(ByContextSelector byContextSelector) {
            this.byContextSelector = byContextSelector;
        }

        @Override
        public void visit(CompositeSelector events) {
            events.eachAccept(this);
        }

        ByLogSelector extract() {
            if (byContextSelector == null || byLogIdSelector == null) {
                return null;
            }
            return Events.byLog(byContextSelector.getLogDomain(), byLogIdSelector.getLogId());
        }
    }
}
