package de.tuberlin.tfdacmacs.centralserver.user.db;

import de.tuberlin.tfdacmacs.basics.db.CouchbaseDB;
import de.tuberlin.tfdacmacs.centralserver.user.data.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserDB extends CouchbaseDB<User> {

    @Autowired
    public UserDB(UserRepository repository) {
        super(repository, User.class);
    }
}
