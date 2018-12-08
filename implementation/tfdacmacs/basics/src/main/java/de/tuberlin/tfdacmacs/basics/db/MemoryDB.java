package de.tuberlin.tfdacmacs.basics.db;

import de.tuberlin.tfdacmacs.basics.db.exception.EntityDoesExistException;
import de.tuberlin.tfdacmacs.basics.db.exception.EntityDoesNotExistException;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.DirectFieldBindingResult;
import org.springframework.validation.SmartValidator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("unused")
public abstract class MemoryDB<T extends Entity> {

    @Autowired
    private SmartValidator validator;
    @Autowired
    private DirectFieldBindingResult directFieldBindingResult;

    private Map<String, T> memoryDB = new HashMap<>();

    /**
     * Finds a entity identified by the given id.
     *
     * @param id Notnull: id of the entity
     * @return optional of the found value or {@link Optional#empty()} if the value was not present
     */
    public Optional<T> findEntity(@NonNull String id) {
        return Optional.ofNullable(memoryDB.get(id));
    }

    /**
     * Inserts the given entity into the database.
     *
     * @param entity NotNull: the entity
     * @return the id of the entity as reference
     * @throws EntityDoesExistException if an entity with the id does exist
     */
    public String insert(@NonNull T entity) throws EntityDoesExistException {
        if(exist(entity.getId())) {
            throw new EntityDoesExistException(String.format("Entity with id [%s] does exist!", entity.getId()));
        } else {
            return upsert(entity);
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
        if(! exist(entity.getId())) {
            throw  new EntityDoesNotExistException(String.format("Entity with id [%s] does not exist!", entity.getId()));
        } else {
            return upsert(entity);
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
        validator.validate(entity, directFieldBindingResult);
        memoryDB.put(entity.getId(), entity);
        return entity.getId();
    }

    /**
     * Finds all presented value in the memory.
     *
     * @return a collection of all found entities.
     */
    public Collection<T> findAll() {
        return memoryDB.values();
    }

    /**
     * Checks whether the given id exist.
     *
     * @param id NotNull: the id
     * @return true if an entity with the id exist, else false.
     */
    public boolean exist(@NonNull String id) {
        return memoryDB.containsKey(id);
    }

    /**
     * Removes the entity with the given id from the database.
     * If the value is not present nothing is removed.
     *
     * @param id NotNull: the id if the entity.
     * @return the removed entity or {@link Optional#empty()} if no entity with the id was found
     */
    public Optional<T> remove(@NonNull String id) {
        return Optional.ofNullable(memoryDB.remove(id));
    }

    /**
     * Drop the whole database.
     */
    public void drop() {
        this.memoryDB = new HashMap<>();
    }
}

