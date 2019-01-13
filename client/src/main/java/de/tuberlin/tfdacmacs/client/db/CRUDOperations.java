package de.tuberlin.tfdacmacs.client.db;

import lombok.NonNull;

import java.util.Optional;
import java.util.stream.Stream;

public interface CRUDOperations<K extends String, T> {

    void insert(@NonNull K key, @NonNull T entity);

    Optional<T> find(@NonNull K key);

    Stream<T> findAll();

    void update(@NonNull K key, @NonNull T entity);

    boolean exist(@NonNull K key);

    void delete(@NonNull K key);

    default void upsert(@NonNull K key, @NonNull T entity) {
        if(exist(key)) {
            update(key, entity);
        } else {
            insert(key, entity);
        }
    }
}
