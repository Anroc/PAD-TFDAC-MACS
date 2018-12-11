package de.tuberlin.tfdacmacs.centralserver.user.db;

import de.tuberlin.tfdacmacs.basics.db.MemoryDB;
import de.tuberlin.tfdacmacs.centralserver.user.data.User;
import org.springframework.stereotype.Component;

@Component
public class UserDB extends MemoryDB<User> {
}
