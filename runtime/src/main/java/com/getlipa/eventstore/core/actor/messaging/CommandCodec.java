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
public class CommandCodec extends ProtoCodec<Command<Message>, Actors.Command> {

    public static final String NAME = "actor-command";

    private final PayloadParser parser;

    @Override
    public String name() {
        return NAME;
    }

    protected Actors.Command toProto(Command<Message> command) {
        return Actors.Command.newBuilder()
                .setId(ProtoUtil.convert(command.getId()))
                .setCausationId(ProtoUtil.convert(command.getCausationId()))
                .setCorrelationId(ProtoUtil.convert(command.getCorrelationId()))
                .setPayload(ProtoUtil.convert(command.getPayload()))
                .build();
    }

    @Override
    protected Command<Message> toWrapped(Actors.Command command) {
        return Command.create()
                .withLazyPayload(() -> parser.parse(command.getPayload()));
    }
}
