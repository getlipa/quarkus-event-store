package com.getlipa.event.store.it;

import com.getlipa.eventstore.core.actor.cdi.Actor;
import com.getlipa.eventstore.core.actor.messaging.Command;
import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.example.event.Example;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Actor("demo")
public class DemoActor {

    private int count = 0;

    public Example.Simple doSomething(Command<Example.Simple> simpleCommand) {
        log.error("Simple Command received: {}", simpleCommand);
        return Example.Simple.newBuilder().setData("replyyyy " + count++).build();
    }

    public Example.Simple handleEvent(Event<Example.Simple> simpleCommand) {
        log.error("EVENT received: {}", simpleCommand);
        return Example.Simple.newBuilder().setData("replyyyy").build();

    }

    public String doFail(Command<Example.Other> simpleCommand) {
        return "HAHA";
    }

}
