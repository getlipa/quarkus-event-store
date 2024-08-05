package com.getlipa.eventstore.it.aggregate;

import com.getlipa.event.store.it.AccountBalance;
import com.getlipa.event.store.it.TotalBalance;
import com.getlipa.eventstore.EventStore;
import com.getlipa.eventstore.aggregate.Aggregates;
import com.getlipa.eventstore.event.Events;
import com.getlipa.eventstore.identifier.Id;
import com.getlipa.eventstore.it.BankAccount;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@Disabled // FIXME: Works only independently due to persistence
public class AggregatesTest {

    @Inject
    Aggregates<AccountBalance> accountBalanceAggregates;

    @Inject
    Aggregates<TotalBalance> totalBalanceAggregates;

    @Inject
    EventStore eventStore;


    @MethodSource
    @ParameterizedTest
    void withContext(int account, List<Double> deposits, double expectedBalance, long expectedRevision) throws ExecutionException, InterruptedException {
        final var accountId = Id.numeric(account);
        final var stream = eventStore.stream(Events.byLog("bank-account", accountId));
        for (final var deposit : deposits) {
            stream.append().withPayload(BankAccount.FundsDeposited.newBuilder()
                            .setAmount(deposit)
                            .build())
                    .toCompletionStage()
                    .toCompletableFuture()
                    .join();
        }

        final var aggregate = accountBalanceAggregates.get(accountId);

        assertEquals(-1L, aggregate.getRevision()); // FIXME: make future?

        final var balance = aggregate.get()
                .map(AccountBalance::getBalance)
                .toCompletionStage()
                .toCompletableFuture()
                .get();

        assertEquals(expectedBalance, balance);
        assertEquals(expectedRevision, aggregate.getRevision());
        assertEquals(accountId, aggregate.getId());
    }

    private static Stream<Arguments> withContext() {
        return Stream.of(
                Arguments.of(1, List.of(), 0d, -1L),
                Arguments.of(2, List.of(100d, 200d), 300d, 2L),
                Arguments.of(3, List.of(10d, 40d, 60d), 110d, 5L)
        );
    }

    @MethodSource
    @ParameterizedTest
    @Disabled // FIXME: Works only independently due to persistence
    void withoutContext(int client, Map<Integer, List<Double>> deposits, double expectedBalance, long expectedRevision) throws ExecutionException, InterruptedException {
        final var clientId = Id.numeric(client);
        for (final var depositsByAccount : deposits.entrySet()) {
            final var aggregateId = Id.numeric(depositsByAccount.getKey());
            final var stream = eventStore.stream(Events.byLog("bank-account", aggregateId));
            for (final var deposit : depositsByAccount.getValue()) {
                stream.append()
                        .withCorrelationId(clientId)
                        .withPayload(BankAccount.FundsDeposited.newBuilder()
                                .setAmount(deposit)
                                .build())
                        .toCompletionStage()
                        .toCompletableFuture()
                        .join();
            }
        }

        final var aggregate = totalBalanceAggregates.get(clientId);

        assertEquals(-1L, aggregate.getRevision()); // FIXME: make future?

        final var balance = aggregate.get()
                .map(TotalBalance::getTotalBalance)
                .toCompletionStage()
                .toCompletableFuture()
                .get();

        assertEquals(expectedBalance, balance);
        assertEquals(expectedRevision, aggregate.getRevision());
        assertEquals(clientId, aggregate.getId());
    }

    private static Stream<Arguments> withoutContext() {
        return Stream.of(
                Arguments.of(1, Map.of(), 0d, -1L),
                Arguments.of(2, Map.of(201, List.of(100d, 200d)), 300d, 2L),
                Arguments.of(3, Map.of(301, List.of(10d, 40d, 60d)), 110d, 5L),
                Arguments.of(4, Map.of(301, List.of(10d, 40d), 302, List.of( 100d, 60d)), 210d, 9L)
        );
    }
}
