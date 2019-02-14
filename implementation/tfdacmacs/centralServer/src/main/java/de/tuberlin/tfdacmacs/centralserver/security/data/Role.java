package de.tuberlin.tfdacmacs.centralserver.security.data;

import lombok.Getter;

import java.lang.reflect.Array;
import java.util.Arrays;

public enum Role {

    USER("ROLE_USER"), AUTHORITY("ROLE_AUTHORITY"), EXTERN("ROLE_EXTERN"), ADMIN("ROLE_ADMIN");

    @Getter
    private final String roleName;

    Role(String roleName) {
        this.roleName = roleName;
    }

    public static Role parse(String roleName) {
        return Arrays.stream(Role.values())
                .filter(role -> role.getRoleName().equals(roleName))
                .findAny()
                .orElseThrow(
                        () -> new IllegalArgumentException("Unknown role name: " + roleName)
                );
    }
}
