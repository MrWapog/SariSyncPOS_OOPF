package sarisync.repositories;

import sarisync.models.Transaction;

import java.util.Optional;

/**
 * Public facade for the in-memory transaction repository.
 * Extends InMemoryTransactionRepositoryImpl to expose it at package level.
 */
public class InMemoryTransactionRepository extends sarisync.repositories.InMemoryTransactionRepositoryImpl {

    /**
     * Overrides to make findLastCompleted publicly accessible.
     * The base class already implements this method — no extra code needed.
     */
    @Override
    public Optional<Transaction> findLastCompleted() {
        return super.findLastCompleted();
    }
}
