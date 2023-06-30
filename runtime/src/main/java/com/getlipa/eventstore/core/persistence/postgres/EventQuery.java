package com.getlipa.eventstore.core.persistence.postgres;

import com.getlipa.eventstore.core.persistence.postgres.query.Sorting;
import com.getlipa.eventstore.core.proto.ProtoUtil;
import com.getlipa.eventstore.core.stream.options.Cursor;
import com.getlipa.eventstore.core.stream.options.PositionCursor;
import com.getlipa.eventstore.core.stream.options.ReadOptions;
import com.getlipa.eventstore.core.stream.options.SeriesIndexCursor;
import com.getlipa.eventstore.core.stream.selector.ByStreamSelector;
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

    public static EventQuery create(ByStreamSelector selector, ReadOptions readOptions) {
        return Builder.create(selector, readOptions);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder implements Cursor.Visitor {

        private final StringBuilder query = new StringBuilder();

        private final Sorting direction;

        private Parameters parameters;

        static EventQuery create(ByStreamSelector selector, ReadOptions readOptions) {
            final var factory = new Builder(Sorting.valueOf(readOptions.direction()));
            factory.query.append("seriesType = :seriesType AND seriesId = :seriesId");
            factory.parameters = Parameters.with("seriesType", ProtoUtil.toUUID(selector.getSeriesType()))
                    .and("seriesId", selector.getSeriesId());
            return create(factory, readOptions);
        }

        static EventQuery create(Builder builder, ReadOptions readOptions) {
            readOptions.startAt().accept(builder);
            return new EventQuery(
                    builder.query.toString(),
                    builder.direction.getSort(),
                    builder.parameters
            );
        }

        @Override
        public void visit(SeriesIndexCursor cursor) {
            query.append(String.format(" and seriesIndex %s :seriesIndex", direction.getOperator()));
            parameters = parameters.and("seriesIndex", cursor.getSeriesIndex());
        }

        @Override
        public void visit(PositionCursor cursor) {
            query.append(String.format(" and position %s :position", direction.getOperator()));
            parameters = parameters.and("position", cursor.getPosition());
        }
    }
}
