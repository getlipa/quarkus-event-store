package com.getlipa.eventstore.core.actor.messaging;

public interface MessageHandler {

    Object handle(Command<?> message);

}
