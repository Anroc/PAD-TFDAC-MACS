package de.tuberlin.tfdacmacs.centralserver.user;

import de.tuberlin.tfdacmacs.centralserver.user.data.User;
import de.tuberlin.tfdacmacs.centralserver.user.db.UserDB;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserDB userDB;

    public User insertUser(@NonNull User user) {
        userDB.insert(user);
        return user;
    }

    public User updateUser(@NonNull User user) {
        userDB.update(user);
        return user;
    }

    public Optional<User> findUser(@NonNull String userId) {
        return userDB.findEntity(userId);
    }

    public List<User> findUsersByAuthorityId(@NonNull String authorityId) {
        return userDB.findUsersByAuthorityId(authorityId);
    }
}
