package sarisync.interfaces;

import java.util.List;
import java.util.Optional;

/**
 * ABSTRACTION 
 * @param <T>  Entity type (Product, Transaction, User, …)
 * @param <ID> Identifier type (usually String for UUID)
 */
public interface IRepository<T, ID> {

    /** Persists a new entity. Returns the saved entity (may have generated fields). */
    T save(T entity);

    /** Returns an entity by its primary key, or empty if not found. */
    Optional<T> findById(ID id);

    /** Returns all entities in the repository. */
    List<T> findAll();

    /** Updates an existing entity. Throws if not found. */
    T update(T entity);

    /** Removes an entity by ID. Returns true if it existed. */
    boolean deleteById(ID id);

    /** Returns the total number of stored entities. */
    int count();
}
