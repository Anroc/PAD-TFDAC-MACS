package de.tuberlin.tfdacmacs.centralserver.user.data;

import de.tuberlin.tfdacmacs.basics.db.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends Entity {

    public User(@NonNull String id) {
        super(id);
    }
}
