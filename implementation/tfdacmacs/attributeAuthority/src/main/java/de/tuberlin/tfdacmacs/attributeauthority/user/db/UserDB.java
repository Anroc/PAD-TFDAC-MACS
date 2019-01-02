package de.tuberlin.tfdacmacs.attributeauthority.user.db;

import de.tuberlin.tfdacmacs.attributeauthority.user.data.User;
import de.tuberlin.tfdacmacs.lib.db.CouchbaseDB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserDB extends CouchbaseDB<User> {
    @Autowired
    public UserDB(UserRepository repository) {
        super(repository, User.class);
    }

}
