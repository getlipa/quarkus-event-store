package com.getlipa.eventstore.core.event.selector;

import com.getlipa.eventstore.core.event.AnyEvent;

public interface Selector {

    boolean matches(AnyEvent event);

    void accept(Visitor visitor);

    static Selector all(Selector... matchers) {
        return new CompositeSelector(matchers);
    }

    interface Visitor {

        default void visit(AllSelector events) {
            throw new UnsupportedOperationException();
        }

        default void visit(ByCausationIdSelector events) {
            throw new UnsupportedOperationException();
        }

        default void visit(ByCorrelationIdSelector events) {
            throw new UnsupportedOperationException();
        }

        default void visit(ByLogIdSelector events) {
            throw new UnsupportedOperationException();
        }

        default void visit(ByLogDomainSelector events) {
            throw new UnsupportedOperationException();
        }

        default void visit(ByLogSelector events) {
            throw new UnsupportedOperationException();
        }

        default void visit(ByTypeSelector events) {
            throw new UnsupportedOperationException();
        }

        default void visit(CompositeSelector events) {
            throw new UnsupportedOperationException();
        }
    }

    interface IgnorantVisitor extends Visitor {
        default void visit(AllSelector events) {}

        default void visit(ByCausationIdSelector events) {}

        default void visit(ByCorrelationIdSelector events) {}

        default void visit(ByLogIdSelector events) {}

        default void visit(ByLogDomainSelector events) {}

        default void visit(ByLogSelector events) {}

        default void visit(ByTypeSelector events) {}

        default void visit(CompositeSelector events) {}
    }
}
