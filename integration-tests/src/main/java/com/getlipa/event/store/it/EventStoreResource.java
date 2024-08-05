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

import com.getlipa.eventstore.EventStore;
import com.getlipa.eventstore.aggregate.Logs;
import com.getlipa.eventstore.event.Event;
import com.getlipa.eventstore.identifier.Id;
import com.getlipa.eventstore.it.BankAccount;
import com.getlipa.eventstore.persistence.exception.EventAppendException;
import com.getlipa.eventstore.event.logindex.LogIndex;
import com.getlipa.eventstore.event.Events;
import com.getlipa.eventstore.aggregate.Log;
import com.getlipa.eventstore.aggregate.Aggregate;
import com.getlipa.eventstore.subscriptions.Projections;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

@Slf4j
@Path("/account/{id}")
@ApplicationScoped
@RequiredArgsConstructor
public class EventStoreResource {

    @Inject
    Logs<AccountBalance> logs;

    @GET
    public Uni<Double> balance(@PathParam("id") String id) throws ExecutionException, InterruptedException {
        return Uni.createFrom()
                .completionStage(logs.get(Id.derive(id))
                        .get()
                        .map(AccountBalance::getBalance)
                        .toCompletionStage()
                );
    }

    @GET
    @Path("deposit")
    public Uni<String> deposit(@PathParam("id") String id) throws ExecutionException, InterruptedException {
        final var future = logs.get(Id.derive(id))
                .append(LogIndex.atAny())
                .withPayload(BankAccount.FundsDeposited.newBuilder()
                        .setAmount(100)
                        .build())
                .map(event -> event.getId().toString());
        return Uni.createFrom().completionStage(future.toCompletionStage());
    }
}
