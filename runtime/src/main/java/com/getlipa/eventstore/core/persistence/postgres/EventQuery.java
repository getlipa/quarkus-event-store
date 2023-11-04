package com.getlipa.eventstore.core.persistence.postgres;

import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.event.selector.*;
import com.getlipa.eventstore.core.persistence.postgres.query.Sorting;
import com.getlipa.eventstore.core.persistence.postgres.query.StartAtVisitor;
import com.getlipa.eventstore.core.persistence.postgres.query.UntilVisitor;
import com.getlipa.eventstore.core.proto.ProtoUtil;
import com.getlipa.eventstore.core.stream.reader.cursor.PositionCursor;
import com.getlipa.eventstore.core.stream.reader.ReadOptions;
import com.getlipa.eventstore.core.stream.reader.cursor.LogIndexCursor;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class EventQuery {

    private static final Sort SORT_ASC = Sort.ascending("position");

    private static final Sort SORT_DESC = Sort.descending("position");

    private final String query;

    private final Sort sort;

    private final Parameters parameters;

    public static EventQuery create(Selector selector, ReadOptions readOptions) {
        return Builder.create(selector, readOptions);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder implements StartAtVisitor, UntilVisitor, Selector.Visitor {

        private final StringBuilder query = new StringBuilder();

        private final Sorting direction;

        private Parameters parameters;

        static EventQuery create(Selector selector, ReadOptions readOptions) {
            final var factory = new Builder(Sorting.valueOf(readOptions.direction()));
            selector.accept(factory);
            return create(factory, readOptions);
        }

        @Override
        public void visit(ByLogSelector selector) {
            query.append("logDomainUuid = :logDomainUuid AND logId = :logId");
            parameters = Parameters.with("logDomainUuid", ProtoUtil.toUUID(Event.EVENT_LOG_DOMAIN_NAMESPACE, selector.getLogDomain()))
                    .and("logId", selector.getLogId());
        }

        @Override
        public void visit(CompositeSelector compositeSelector) {
            compositeSelector.eachAccept(this);
        }

        @Override
        public void visit(ByLogDomainSelector selector) {
            addCondition("logDomainUuid = :logDomainUuid");
            addParameter("logDomainUuid", ProtoUtil.toUUID(Event.EVENT_LOG_DOMAIN_NAMESPACE, selector.getLogDomain()));
        }

        @Override
        public void visit(ByLogIdSelector selector) {
            addCondition("logId = :logId");
            addParameter("logId", selector.getLogId());
        }

        private void addCondition(final String condition) {
            if (query.length() > 0) {
                query.append(" AND ");
            }
            query.append(condition);
        }

        private void addParameter(final String name, final Object value) {
            if (parameters == null) {
                parameters = Parameters.with(name, value);
                return;
            }
            parameters = parameters.and(name, value);
        }

        static EventQuery create(Builder builder, ReadOptions readOptions) {
            readOptions.from().accept((StartAtVisitor) builder);
            readOptions.until().accept((UntilVisitor) builder);
            return new EventQuery(
                    builder.query.toString(),
                    builder.direction.getSort(),
                    builder.parameters
            );
        }

        @Override
        public void visitStartAt(LogIndexCursor cursor) {
            query.append(String.format(" and logIndex %s :startAt", direction.getOperator()));
            parameters = parameters.and("startAt", cursor.getLogIndex());
        }

        @Override
        public void visitStartAt(PositionCursor cursor) {
            query.append(String.format(" and position %s :startAt", direction.getOperator()));
            parameters = parameters.and("startAt", cursor.getPosition());
        }

        @Override
        public void visitUntil(LogIndexCursor cursor) {
            query.append(String.format(" and position %s :until", direction.getOppositeOperator()));
            parameters = parameters.and("until", cursor.getLogIndex());
        }

        @Override
        public void visitUntil(PositionCursor cursor) {
            query.append(String.format(" and position %s :until", direction.getOppositeOperator()));
            parameters = parameters.and("until", cursor.getPosition());
        }
    }
}
