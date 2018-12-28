package de.tuberlin.tfdacmacs.attributeauthority.user.db;

import com.couchbase.client.java.Bucket;
import de.tuberlin.tfdacmacs.attributeauthority.user.data.User;
import de.tuberlin.tfdacmacs.basics.db.CouchbaseDB;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserDB extends CouchbaseDB<User> {
    public UserDB(Bucket bucket,
            UserRepository repository,
            CouchbaseTemplate template) {
        super(bucket, repository, template, User.class);
    }
}
