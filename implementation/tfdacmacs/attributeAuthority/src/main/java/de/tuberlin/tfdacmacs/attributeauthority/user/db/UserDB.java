package de.tuberlin.tfdacmacs.attributeauthority.user.db;

import de.tuberlin.tfdacmacs.attributeauthority.user.data.User;
import de.tuberlin.tfdacmacs.lib.db.CouchbaseDB;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Stream;

@Component
public class UserDB extends CouchbaseDB<User> {

    private final UserRepository repository;

    @Autowired
    public UserDB(UserRepository repository) {
        super(repository, User.class);
        this.repository = repository;
    }

    @Override
    public Optional<User> findEntity(@NonNull String id) {
        return repository.findUserById(id);
    }

    @Override
    public boolean exist(@NonNull String id) {
        return findEntity(id).isPresent();
    }

    public Stream<User> findUsersByAttributeIdAndValue(@NonNull String attributeId, @NonNull String value) {
        return repository.findUsersByAttributeIdAndValue(attributeId, value);
    }
}
