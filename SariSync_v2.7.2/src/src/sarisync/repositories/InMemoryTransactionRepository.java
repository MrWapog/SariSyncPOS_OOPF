package sarisync.repositories;

import sarisync.models.Transaction;

import java.util.Optional;

/**
 * INHERITANCE — extends InMemoryTransactionRepositoryImpl
 *
 * v2.6.0: adds findByNumber() so TransactionService.voidTransaction()
 * can locate a transaction by its human-readable TXN-XXXXXXXX-NNNN number.
 */
public class InMemoryTransactionRepository extends InMemoryTransactionRepositoryImpl {

    @Override
    public Optional<Transaction> findLastCompleted() {
        return super.findLastCompleted();
    }

    /**
     * Finds a transaction by its transaction NUMBER (e.g. "TXN-20260519-0001").
     * The number is different from the UUID primary key.
     */
    public Optional<Transaction> findByNumber(String transactionNumber) {
        return findAll().stream()
            .filter(t -> t.getTransactionNumber().equals(transactionNumber))
            .findFirst();
    }
}
