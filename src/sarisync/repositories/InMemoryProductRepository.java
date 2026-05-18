package sarisync.repositories;

import sarisync.models.Product;
import sarisync.models.Transaction;

import java.util.Optional;

/**
 * Public facade for the in-memory product repository.
 * Extends InMemoryProductRepositoryImpl to expose it at package level.
 */
public class InMemoryProductRepository extends sarisync.repositories.InMemoryProductRepositoryImpl {
    // All behaviour inherited from InMemoryProductRepositoryImpl.
    // Additional query methods can be added here as the system grows.
    // Will be use in future update. Antok nako
}
