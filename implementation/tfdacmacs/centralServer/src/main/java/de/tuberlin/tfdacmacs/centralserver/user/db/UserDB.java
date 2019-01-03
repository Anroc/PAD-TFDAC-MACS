package de.tuberlin.tfdacmacs.centralserver.user.db;

import de.tuberlin.tfdacmacs.lib.db.CouchbaseDB;
import de.tuberlin.tfdacmacs.centralserver.user.data.User;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;

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
}
