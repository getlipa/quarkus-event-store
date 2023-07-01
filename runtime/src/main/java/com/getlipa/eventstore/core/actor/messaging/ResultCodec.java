package com.getlipa.eventstore.core.actor.messaging;

import com.getlipa.eventstore.actors.Actors;
import com.getlipa.eventstore.core.proto.PayloadParser;
import com.getlipa.eventstore.core.proto.ProtoCodec;
import com.getlipa.eventstore.core.proto.ProtoUtil;
import com.google.protobuf.Message;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class ResultCodec extends ProtoCodec<Result<Message>, Actors.Result> {

    public static final String NAME = "actor-command-result";

    private final PayloadParser parser;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    protected Actors.Result toProto(Result<Message> result) {
        return Actors.Result.newBuilder()
                .setId(ProtoUtil.convert(result.getId()))
                .setCausationId(ProtoUtil.convert(result.getCausationId()))
                .setCorrelationId(ProtoUtil.convert(result.getCorrelationId()))
                .setPayload(ProtoUtil.convert(result.getPayload()))
                .build();
    }

    @Override
    protected Result<Message> toWrapped(Actors.Result result) {
        return Result.create()
                .withLazyPayload(() -> parser.parse(result.getPayload()));
    }
}

