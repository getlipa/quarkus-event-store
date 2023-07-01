package com.getlipa.eventstore.core.persistence.postgres;

import com.getlipa.eventstore.core.event.EphemeralEvent;
import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.event.seriesindex.SeriesIndex;
import com.getlipa.eventstore.core.persistence.EventPersistence;
import com.getlipa.eventstore.core.persistence.exception.EventAppendException;
import com.getlipa.eventstore.core.stream.selector.ByStreamSelector;
import com.google.protobuf.Message;
import jakarta.inject.Inject;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class JtaEventPersistence<T> implements EventPersistence {

    @Inject
    TransactionManager transactionManager;

    @Override
    public <T extends Message> Event<T> append(ByStreamSelector selector, SeriesIndex seriesIndex, EphemeralEvent<T> event) throws EventAppendException {
        try {
            final var suspendedTransaction = beginTransaction();
            final var persisted = doAppend(selector, seriesIndex, event);
            commitTransaction(suspendedTransaction);
            return persisted;
        } catch (EventAppendException e) {
            throw e;
        } catch (Throwable e) {
            log.info("Unable to append event '{}': {}", e, e.getMessage());
            rollbackTransaction();
            throw EventAppendException.from(e);
        }
    }

    protected abstract void handleRollback(RollbackException rollbackException) throws EventAppendException;

    protected abstract <T extends Message> Event<T> doAppend(ByStreamSelector selector, SeriesIndex seriesIndex, EphemeralEvent<T> event) throws EventAppendException;

    private Transaction beginTransaction() throws SystemException, NotSupportedException {
        Transaction currentTransaction = null;
        if (transactionManager.getStatus() != Status.STATUS_NO_TRANSACTION) {
            currentTransaction = transactionManager.suspend();
        }
        transactionManager.begin();
        return currentTransaction;
    }

    private void commitTransaction(Transaction suspendedTransaction) throws SystemException, EventAppendException {
        try {
            transactionManager.commit();
        } catch (RollbackException e) {
            handleRollback(e);
            log.error("Transaction commit failed and rolled back: " + e.getMessage());
        } catch (HeuristicMixedException e) {
            log.warn(
                    "Some transitions have been persisted and others have been rolled back due to a heuristic decision."
            );
        } catch (HeuristicRollbackException e) {
            log.warn(
                    "All transitions have been rolled back due to a heuristic decision."
            );
        }
        if (suspendedTransaction != null) {
            try {
                transactionManager.resume(suspendedTransaction);
            } catch (InvalidTransactionException e) {
                throw new IllegalStateException("Unable to resume previous transaction: " + e.getMessage());
            }
        }
    }

    private void rollbackTransaction() {
        try {
            transactionManager.rollback();
        } catch (SystemException e) {
            log.error("Unable to rollback transaction: " + e.getMessage());
        }
    }
}
