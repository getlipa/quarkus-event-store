package com.getlipa.eventstore.core.subscription;

import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.proto.PayloadParser;
import com.getlipa.eventstore.core.proto.ProtoCodec;
import com.getlipa.eventstore.core.proto.ProtoUtil;
import com.getlipa.eventstore.subscriptions.Subscriptions;
import com.google.protobuf.Message;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@RequiredArgsConstructor
public class EventCodec extends ProtoCodec<Event<Message>, Subscriptions.Event> {

    public static final String NAME = "event";

    private final PayloadParser parser;

    @Override
    public String name() {
        return NAME;
    }

    public static Subscriptions.Event protoFrom(Event<?> event) {
        return Subscriptions.Event.newBuilder()
                .setId(ProtoUtil.convert(event.getId()))
                .setPosition(event.getPosition())
                .setSeriesIndex(event.getSeriesIndex())
                .setSeriesType(ProtoUtil.convert(event.getSeriesType()))
                .setSeriesId(ProtoUtil.convert(event.getSeriesId()))
                .setCreatedAt(ProtoUtil.convert(event.getCreatedAt()))
                .setCausationId(ProtoUtil.convert(event.getCausationId()))
                .setCorrelationId(ProtoUtil.convert(event.getCorrelationId()))
                .setPayload(ProtoUtil.convert(event.getPayload()))
                .build();
    }

    protected Subscriptions.Event toProto(Event<Message> wrapped) {
        return protoFrom(wrapped);
    }

    public static Event<Message> wrappedFrom(Subscriptions.Event event, PayloadParser parser) {
        return Event.builder()
                .id(ProtoUtil.toUUID(event.getId()))
                .position(event.getPosition())
                .seriesIndex(event.getSeriesIndex())
                .seriesType(ProtoUtil.toUUID(event.getSeriesType()))
                .seriesId(ProtoUtil.toUUID(event.getSeriesId()))
                .causationId(ProtoUtil.toUUID(event.getCausationId()))
                .correlationId(ProtoUtil.toUUID(event.getCorrelationId()))
                .createdAt(OffsetDateTime.ofInstant(Instant.ofEpochSecond(
                        event.getCreatedAt().getSeconds(),
                        event.getCreatedAt().getNanos()), ZoneId.of("UTC"))
                )
                .payload(() -> parser.parse(event.getPayload()))
                .build();
    }

    @Override
    protected Event<Message> toWrapped(Subscriptions.Event event) {
        return wrappedFrom(event, parser);
    }
}
