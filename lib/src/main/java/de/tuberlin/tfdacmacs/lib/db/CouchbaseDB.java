package de.tuberlin.tfdacmacs.lib.db;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.error.DocumentAlreadyExistsException;
import com.couchbase.client.java.error.DocumentDoesNotExistException;
import com.google.common.collect.ImmutableList;
import de.tuberlin.tfdacmacs.lib.db.exception.EntityDoesExistException;
import de.tuberlin.tfdacmacs.lib.db.exception.EntityDoesNotExistException;
import de.tuberlin.tfdacmacs.lib.events.DomainEvent;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.data.couchbase.repository.CouchbaseRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

@SuppressWarnings("unused")
@Slf4j
@RequiredArgsConstructor
public abstract class CouchbaseDB<T extends Entity> {

    @Autowired
    private Bucket bucket;
    @Autowired
    private CouchbaseTemplate template;
    @Autowired
    private ApplicationEventPublisher publisher;

    private final CouchbaseRepository<T, String> repository;
    private final Class<T> clazz;

    @Getter
    private final Set<String> ids = new HashSet<>();

    /**
     * Finds a entity identified by the given id.
     *
     * @param id Notnull: id of the entity
     * @return optional of the found value or {@link Optional#empty()} if the value was not present
     */
    public Optional<T> findEntity(@NonNull String id) {
        try {
            return Optional.ofNullable(template.findById(id, clazz));
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
            call(entity, template::insert);
            ids.add(entity.getId());
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
            call(entity, template::update);
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
     * Checks whether the given id exist.
     * <b>Does not check for class value</b>
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
        return findEntity(id).map(this::remove);
    }

    /**
     * Removes the given entity from the database.
     *
     * @param entity the entity preserved in the database
     * @return the entity
     */
    public T remove(@NonNull T entity) {
        call(entity, repository::delete);
        ids.remove(entity.getId());
        return entity;
    }

    /**
     * Drop the whole database.
     */
    public void drop() {
        try {
            ids.forEach(bucket::remove);
        } catch (DocumentDoesNotExistException e) {
        } finally {
            ids.clear();
        }
    }

    public void call(T entity, Consumer<T> dbCall) {
        List<DomainEvent> events = ImmutableList.copyOf(entity.getEvents());
        entity.getEvents().clear();
        dbCall.accept(entity);
        events.forEach(publisher::publishEvent);
    }
}

