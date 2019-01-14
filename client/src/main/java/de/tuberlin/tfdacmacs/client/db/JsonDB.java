package de.tuberlin.tfdacmacs.client.db;

import com.fasterxml.jackson.databind.Module;
import de.tuberlin.tfdacmacs.client.db.config.JsonDBConfiguration;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Log4j2
@RequiredArgsConstructor
public abstract class JsonDB<T> implements CRUDOperations<String, T> {

    @Autowired
    private JsonDBConfiguration jsonDBConfiguration;

    private final Class<T> clazz;
    private final Map<String, T> memoryMap = new HashMap<>();

    @Getter
    private FileEngine fileEngine;

    @PostConstruct
    public final void init() {
        fileEngine = new FileEngine(clazz, jsonDBConfiguration.getDataDir());
        registerModule();
    }

    @Override
    public void insert(@NonNull String key, @NonNull T entity) {
        fileEngine.insert(key, entity);
        memoryMap.put(key, entity);
    }

    @Override
    public Optional<T> find(@NonNull String key) {
        if (memoryMap.containsKey(key)) {
            return Optional.of(memoryMap.get(key));
        } else {
            Optional<T> optional = fileEngine.find(key);
            if(optional.isPresent()) {
                memoryMap.put(key, optional.get());
            }
            return optional;
        }
    }

    @Override
    public Stream<T> findAll() {
        return fileEngine.findAll();
    }

    @Override
    public void update(@NonNull String key, @NonNull T entity) {
        fileEngine.update(key, entity);
        memoryMap.put(key, entity);
    }

    @Override
    public boolean exist(@NonNull String key) {
        if(! memoryMap.containsKey(key)) {
            return fileEngine.exist(key);
        } else {
            return true;
        }
    }

    @Override
    public void delete(@NonNull String key) {
        fileEngine.delete(key);
        memoryMap.remove(key);
    }

    @Override
    public void drop() {
        fileEngine.drop();
        memoryMap.clear();
    }

    /**
     * Can be overwritten to register custom modules.
     *
     * @return array of modules. Default is Module[0]
     */
    public Module[] getCustomModule() {
        return new Module[0];
    }

    private final void registerModule() {
        Arrays.stream(getCustomModule()).forEach(fileEngine::registerModule);
    }
}
