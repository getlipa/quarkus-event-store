package com.getlipa.eventstore.it.aggregate;

import com.getlipa.event.store.it.AccountBalance;
import com.getlipa.eventstore.EventStore;
import com.getlipa.eventstore.aggregate.Logs;
import com.getlipa.eventstore.event.Events;
import com.getlipa.eventstore.identifier.Id;
import com.getlipa.eventstore.it.BankAccount;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class LogsTest {

    @Inject
    Logs<AccountBalance> accountBalanceLogs;

    @MethodSource
    @ParameterizedTest
    void withContext(int account, List<Double> deposits, double expectedBalance, long expectedRevision) throws ExecutionException, InterruptedException {
        final var accountId = Id.numeric(account);
        final var log = accountBalanceLogs.get(accountId);

        assertEquals(-1L, log.getRevision()); // FIXME: make future?

        for (final var deposit : deposits) {
            log.append().withPayload(BankAccount.FundsDeposited.newBuilder()
                            .setAmount(deposit)
                            .build())
                    .toCompletionStage()
                    .toCompletableFuture()
                    .join();
        }


        assertEquals(accountId, log.getId());
        assertEquals(expectedRevision, log.getRevision());
        final var balance = log.get()
                .map(AccountBalance::getBalance)
                .toCompletionStage()
                .toCompletableFuture()
                .get();

        assertEquals(expectedBalance, balance);
    }

    private static Stream<Arguments> withContext() {
        return Stream.of(
                Arguments.of(1, List.of(), 0d, -1L),
                Arguments.of(2, List.of(100d, 200d), 300d, 1L),
                Arguments.of(3, List.of(10d, 40d, 60d), 110d, 2L)
        );
    }
}
