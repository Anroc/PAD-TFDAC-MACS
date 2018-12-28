package de.tuberlin.tfdacmacs.basics.db;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.error.DocumentAlreadyExistsException;
import com.couchbase.client.java.error.DocumentDoesNotExistException;
import de.tuberlin.tfdacmacs.basics.db.exception.EntityDoesExistException;
import de.tuberlin.tfdacmacs.basics.db.exception.EntityDoesNotExistException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.data.couchbase.repository.CouchbaseRepository;

import java.util.*;

@SuppressWarnings("unused")
@Slf4j
@RequiredArgsConstructor
public abstract class CouchbaseDB<T extends Entity> {

    private final Bucket bucket;
    private final CouchbaseRepository<T, String> repository;
    private final CouchbaseTemplate template;
    private final Class<T> clazz;

    /**
     * Finds a entity identified by the given id.
     *
     * @param id Notnull: id of the entity
     * @return optional of the found value or {@link Optional#empty()} if the value was not present
     */
    public Optional<T> findEntity(@NonNull String id) {
        try {
            return Optional.of(template.findById(id, clazz));
        } catch (DocumentDoesNotExistException e) {
            log.debug("Document with id {} does not exist.", id, e);
            return Optional.empty();
        }
    }

    /**
     * Inserts the given entity into the database.
     *
     * @param entity NotNull: the entity
     * @return the id of the entity as reference
     * @throws EntityDoesExistException if an entity with the id does exist
     */
    public String insert(@NonNull T entity) throws EntityDoesExistException {
        try {
            template.insert(entity);
            return entity.getId();
        } catch (DocumentAlreadyExistsException e) {
            throw new EntityDoesExistException(String.format("Entity with id [%s] does exist!", entity.getId()), e);
        }
    }

    /**
     * Updates a given entity.
     * Existing entity will be replaced.
     *
     * @param entity NotNull: the updated entity
     * @return the key of the entity for reference
     * @throws EntityDoesNotExistException if no entity with the given id was found
     */
    public String update(@NonNull T entity) throws EntityDoesNotExistException {
        try {
            template.update(entity);
            return entity.getId();
        } catch (DocumentDoesNotExistException e) {
            throw new EntityDoesExistException(String.format("Entity with id [%s] does not exist!", entity.getId()), e);
        }
    }

    /**
     * Updates or inserts the given entity.
     * Does not perform any existence checks compared to {@link #insert(Entity)} and {@link #update(Entity)}.
     *
     * @param entity NotNull: the entity that shell be inserted/updated
     * @return the id of the entity for reference
     */
    public String upsert(@NonNull T entity) {
        if(exist(entity.getId())) {
            return update(entity);
        } else {
            return insert(entity);
        }
    }

    /**
     * Finds all presented value in the memory.
     *
     * @return a collection of all found entities.
     */
    public Collection<T> findAll() {
        Collection<T> collection = new ArrayList();
        repository.findAll().forEach(collection::add);
        return collection;
    }

    /**
     * Checks whether the given id exist.
     *
     * @param id NotNull: the id
     * @return true if an entity with the id exist, else false.
     */
    public boolean exist(@NonNull String id) {
        return bucket.exists(id);
    }

    /**
     * Removes the entity with the given id from the database.
     * If the value is not present nothing is removed.
     *
     * @param id NotNull: the id if the entity.
     * @return the removed entity or {@link Optional#empty()} if no entity with the id was found
     */
    public Optional<T> remove(@NonNull String id) {
        Optional<T> entity = findEntity(id);
        if(entity.isPresent()) {
            repository.delete(entity.get());
            return entity;
        } else {
            return Optional.empty();
        }
    }

    /**
     * Drop the whole database.
     */
    public void drop() {
        repository.deleteAll();
    }
}

