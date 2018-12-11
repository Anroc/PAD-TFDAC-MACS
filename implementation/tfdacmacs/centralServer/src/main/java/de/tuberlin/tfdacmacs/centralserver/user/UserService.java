package de.tuberlin.tfdacmacs.centralserver.user;

import de.tuberlin.tfdacmacs.centralserver.key.KeyService;
import de.tuberlin.tfdacmacs.centralserver.user.data.User;
import de.tuberlin.tfdacmacs.centralserver.user.db.UserDB;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserDB userDB;
    private final KeyService keyService;

    public boolean existUser(@NonNull String id) {
        return userDB.exist(id);
    }

    public User createUser(String email) {
        User user = new User(email, keyService.sign(email));
        userDB.insert(user);
        return user;
    }
}
