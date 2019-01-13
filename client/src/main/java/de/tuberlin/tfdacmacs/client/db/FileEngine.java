package de.tuberlin.tfdacmacs.client.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.tuberlin.tfdacmacs.client.db.exception.EntityAlreadyExistException;
import de.tuberlin.tfdacmacs.client.db.models.ElementDeserializer;
import de.tuberlin.tfdacmacs.client.db.models.ElementSerializer;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import lombok.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class FileEngine<T> implements CRUDOperations<String, T> {

    private final Class<T> clazz;
    private final String dataDir;
    private final ObjectMapper objectMapper;

    public FileEngine(Class<T> clazz, String dataDir) {
        this.clazz = clazz;
        this.dataDir = dataDir;

        getPath().toFile().mkdirs();

        this.objectMapper = new ObjectMapper();
    }

    public void configureObjectMapper(Field field) {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Element.class, new ElementDeserializer(field));
        module.addSerializer(Element.class, new ElementSerializer());
        objectMapper.registerModule(module);
    }

    @Override
    public void insert(@NonNull String key, @NonNull T entity) {
        try{
            save(key, entity);
        } catch (FileAlreadyExistsException e) {
            throw new EntityAlreadyExistException(e);
        }
    }

    @Override
    public Optional<T> find(@NonNull String key) {
        return load(key);
    }

    @Override public Stream<T> findAll() {
        return loadAll();
    }

    @Override
    public void update(@NonNull String key, @NonNull T entity) {
        delete(key);
        insert(key, entity);
    }

    @Override
    public boolean exist(@NonNull String key) {
        return Files.exists(getPathFor(key));
    }

    @Override
    public void delete(@NonNull String key) {
        try {
            Files.deleteIfExists(getPathFor(key));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void save(String key, T entity) throws FileAlreadyExistsException {
        try {
            File file = Files.createFile(getPathFor(key)).toFile();
            Files.write(file.toPath(), objectMapper.writeValueAsBytes(entity));
        } catch (IOException e) {
            if(e instanceof FileAlreadyExistsException) {
                throw (FileAlreadyExistsException) e;
            } else {
                throw new UncheckedIOException(e);
            }
        }
    }

    private Optional<T> load(String key) {
        return load(getPathFor(key).toFile());
    }

    private Optional<T> load(File file) {
        try{
            return Optional.of(objectMapper.readValue(file, clazz));
        } catch (IOException e) {
            if(e instanceof FileNotFoundException) {
                return Optional.empty();
            } else {
                throw new UncheckedIOException(e);
            }
        }
    }

    private Stream<T> loadAll() {
        return Arrays.stream(getPath().toFile().listFiles())
                .map(this::load)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private Path getPath() {
        return Paths.get(getBasePath());
    }

    private Path getPathFor(String key) {
        if(key.contains(File.separator)) {
            throw new IllegalArgumentException(
                    String.format("Key is not allowed to contain '%s' but was [%s].", File.separator, key));
        }
        return Paths.get(getBasePath() + File.separator + key + ".json");
    }

    private String getBasePath() {
        return dataDir + File.separator + clazz.getCanonicalName();
    }
}
