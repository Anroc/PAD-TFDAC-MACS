package de.tuberlin.tfdacmacs.centralserver.user.db;

import de.tuberlin.tfdacmacs.lib.db.CouchbaseDB;
import de.tuberlin.tfdacmacs.centralserver.user.data.User;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UserDB extends CouchbaseDB<User> {
    private final UserRepository repository;

    public UserDB(UserRepository repository) {
        super(repository, User.class);
        this.repository = repository;
    }

    public List<User> findUsersByAuthorityId(@NonNull String authorityId) {
        return repository.findUsersByAuthorityId(authorityId);
    }

    public Optional<User> findByIdAndAuthorityId(@NonNull String userId, @NonNull String authorityId) {
        return repository.findByIdAndAuthorityId(userId, authorityId);
    }
}
