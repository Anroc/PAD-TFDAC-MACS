package de.tuberlin.tfdacmacs.attributeauthority.user.db;

import de.tuberlin.tfdacmacs.attributeauthority.user.data.User;
import de.tuberlin.tfdacmacs.basics.db.CouchbaseDB;
import org.springframework.stereotype.Component;

@Component
public class UserDB extends CouchbaseDB<User> {
}
