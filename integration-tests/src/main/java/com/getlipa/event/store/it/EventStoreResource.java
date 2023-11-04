/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.getlipa.event.store.it;

import com.getlipa.eventstore.core.EventStore;
import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.persistence.exception.EventAppendException;
import com.getlipa.eventstore.core.event.logindex.LogIndex;
import com.getlipa.eventstore.core.event.Events;
import com.getlipa.eventstore.core.projection.projected.ProjectedLog;
import com.getlipa.eventstore.core.projection.projected.ProjectedStream;
import com.getlipa.eventstore.example.event.Example;
import com.getlipa.eventstore.subscriptions.Projections;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/event-store")
@ApplicationScoped
@RequiredArgsConstructor
public class EventStoreResource {
    // add some rest methods here

    //@Inject
    //@Any
    ProjectedLog<DemoProjection> projectedLog;

    @Inject
    @Any
    Instance<ProjectedStream<DemoProjection>> projected;


    private final EventStore eventStore;

    @GET
    public String hello() {


        return "Hello event-store - ";
    }

    @GET
    @Path("append")
    public Uni<String> append() throws EventAppendException {
        final var event = Event.withPayload(Example.Simple.newBuilder()
                .setData("test").build());


        return Uni.createFrom().completionStage(eventStore.stream(Events.byLog("gugus", "gugus"))
                .append(LogIndex.atAny(), event)
                .map(e -> String.format("%s", e))
                .toCompletionStage());
    }

    @GET
    @Path("trigger")
    public Uni<String> trigger() throws EventAppendException {
        final var event = Event.withPayload(Projections.CatchUpStarted.newBuilder()
                .build());


        return Uni.createFrom().completionStage(eventStore.stream(Events.byLog("projection-manager", "demo-projection"))
                .append(LogIndex.atAny(), event)
                .map(e -> String.format("%s", e))
                .toCompletionStage()
        );
    }
}
